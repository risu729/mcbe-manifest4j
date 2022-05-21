/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j.gson;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;

// based on a code from javadoc of Gson,
// https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/TypeAdapterFactory.html
class SnakeCaseEnum implements TypeAdapterFactory {
  @Override
  @SuppressWarnings("unchecked")
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<T> rawType = (Class<T>) type.getRawType();
    if (!rawType.isEnum()) {
      return null;
    }

    final Map<T, String> constantToString = new HashMap<T, String>();
    final Map<String, T> stringToConstant = new HashMap<String, T>();
    for (T constant : rawType.getEnumConstants()) {
      final SerializedName annotation;
      try {
        annotation = constant.getClass()
            .getField(((Enum) constant).name())
            .getAnnotation(SerializedName.class);
      } catch (NoSuchFieldException e) {
        throw new UndeclaredThrowableException(e);
      }
      if (annotation == null) {
        String snakeCase = ManifestGson.toSnakeCase(constant.toString());
        constantToString.put(constant, snakeCase);
        stringToConstant.put(snakeCase, constant);
      } else {
        String serializedName = annotation.value();
        constantToString.put(constant, serializedName);
        stringToConstant.put(serializedName, constant);
        for (String alternate : annotation.alternate()) {
          stringToConstant.put(alternate, constant);
        }
      }
    }

    return new TypeAdapter<T>() {
      @Override
      public T read(JsonReader reader) throws IOException {
        return stringToConstant.get(reader.nextString());
      }

      @Override
      public void write(JsonWriter writer, T value) throws IOException {
        writer.value(constantToString.get(value));
      }
    }.nullSafe();
  }
}
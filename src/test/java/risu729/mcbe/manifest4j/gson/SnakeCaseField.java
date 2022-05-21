/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j.gson;

import java.lang.reflect.Field;

import com.google.gson.FieldNamingStrategy;

class SnakeCaseField implements FieldNamingStrategy {

  @Override
  public String translateName(Field f) {
    return ManifestGson.toSnakeCase(f.getName());
  }
}
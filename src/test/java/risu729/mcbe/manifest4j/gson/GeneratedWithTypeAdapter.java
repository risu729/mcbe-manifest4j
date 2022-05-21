/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j.gson;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.TypeAdapter;

import risu729.mcbe.manifest4j.Metadata;
import risu729.mcbe.manifest4j.SemVer;

class GeneratedWithTypeAdapter extends TypeAdapter<Metadata.GeneratedWith> {

  @Override
  public Metadata.GeneratedWith read(JsonReader reader) throws IOException {
    reader.beginObject();
    var builder = new Metadata.GeneratedWith.Builder()
        .name(reader.nextName());
    reader.beginArray();
    while (reader.hasNext()) {
      builder.addVersions(new SemVer(reader.nextString()));
    }
    reader.endArray();
    reader.endObject();
    return builder.build();
  }

  @Override
  public void write(JsonWriter writer, Metadata.GeneratedWith value) throws IOException {
    writer.beginObject()
        .name(value.getName())
        .beginArray();
    for (SemVer semVer : value.getVersions()) {
      writer.value(semVer.toString());
    }
    writer.endArray()
        .endObject();
  }
}
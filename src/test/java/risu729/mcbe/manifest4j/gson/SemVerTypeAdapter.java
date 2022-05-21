/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j.gson;

import java.util.ArrayList;
import java.io.IOException;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.TypeAdapter;

import risu729.mcbe.manifest4j.SemVer;

class SemVerTypeAdapter extends TypeAdapter<SemVer> {

  @Override
  public SemVer read(JsonReader reader) throws IOException {
    List<Integer> semVer = new ArrayList<>(3);
    reader.beginArray();
    while (reader.hasNext()) {
      semVer.add(reader.nextInt());
    }
    reader.endArray();
    return new SemVer(semVer);
  }

  @Override
  public void write(JsonWriter writer, SemVer value) throws IOException {
    writer.beginArray();
    for (int n : value.toArray()) {
      writer.value(n);
    }
    writer.endArray();
  }
}
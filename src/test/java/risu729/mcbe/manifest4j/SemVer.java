/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

// Follows Semantic Versioning 2.0.0 (https://semver.org) but denoting a pre-release version and build metadata is not allowed.

package risu729.mcbe.manifest4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SemVer implements Comparable<SemVer> {

  static final SemVer DEFAULT = new SemVer(1, 0, 0);
  static final SemVer MCBE_DEFAULT = new SemVer(1, 13, 0);

  private int[] semVerArr;

  public SemVer(int... semVer) {
    if (semVer.length == 0 || semVer.length > 3) {
      throw new IllegalArgumentException(
          "invalid semantic versioning : " + Arrays.toString(semVer));
    }
    for (int i : semVer) {
      if (i < 0) {
        throw new IllegalArgumentException(
            "invalid semantic versioning : " + Arrays.toString(semVer));
      }
    }
    this.semVerArr = Arrays.copyOf(semVer, 3);
  }

  public SemVer(List<Integer> semVer) {
    this(semVer.stream()
        .mapToInt(Integer::intValue)
        .toArray());
  }

  public SemVer(String semVer) {
    this(Arrays.stream(validateString(semVer).split("\\."))
        .mapToInt(Integer::parseInt)
        .toArray());
  }

  public int[] toArray() {
    return semVerArr;
  }

  private static String validateString(String str) {
    if (!str.matches("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$")) {
      throw new IllegalArgumentException("malformed semantic verioning : " + str);
    }
    return str;
  }

  @Override
  public int compareTo(SemVer other) {
    return Arrays.compare(semVerArr, other.semVerArr);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    return (obj instanceof SemVer other)
        && Arrays.equals(semVerArr, other.semVerArr);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(semVerArr);
  }

  @Override
  public String toString() {
    return String.join(".",
        Arrays.stream(semVerArr)
            .mapToObj(String::valueOf)
            .toArray(String[]::new));
  }
}
/*
 * Copyright (c) 2022 Risu
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package risu729.mcbe.manifest4j;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Function;
import java.util.Objects;

interface NullsFirstComparator<T> extends Comparator<T> {

  public static <T, U extends Comparable<? super U>> NullsFirstComparator<T> comparing(
      Function<? super T, ? extends U> keyExtractor) {
    return comparing(keyExtractor, Comparator.naturalOrder());
  }

  public static <T, U> NullsFirstComparator<T> comparing(Function<? super T, ? extends U> keyExtractor,
      Comparator<? super U> keyComparator) {
    Objects.requireNonNull(keyExtractor);
    Objects.requireNonNull(keyComparator);
    return (NullsFirstComparator<T> & Serializable) (c1, c2) -> Comparator.nullsFirst(keyComparator)
        .compare(keyExtractor.apply(c1), keyExtractor.apply(c2));
  }

  @Override
  public default NullsFirstComparator<T> thenComparing(Comparator<? super T> other) {
    Objects.requireNonNull(other);
    return (NullsFirstComparator<T> & Serializable) (c1, c2) -> {
      int res = compare(c1, c2);
      return (res != 0) ? res : Comparator.nullsFirst(other).compare(c1, c2);
    };
  }

  @Override
  public default <U extends Comparable<? super U>> NullsFirstComparator<T> thenComparing(
      Function<? super T, ? extends U> keyExtractor) {
    return thenComparing(comparing(keyExtractor));
  }

  @Override
  public default <U> NullsFirstComparator<T> thenComparing(Function<? super T, ? extends U> keyExtractor,
      Comparator<? super U> keyComparator) {
    return thenComparing(comparing(keyExtractor, keyComparator));
  }
}
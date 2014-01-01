/*
 * Wifi Fixer for Android
 *     Copyright (C) 2010-2014  David Van de Ven
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see http://www.gnu.org/licenses
 */

package com.actionbarsherlock.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.actionbarsherlock.internal.ActionBarSherlockCompat.cleanActivityName;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ResourcesCompatTest {
    @Test
    public void testCleanActivityName() {
        assertThat(cleanActivityName("com.jakewharton.test", "com.other.package.SomeClass")) //
            .isEqualTo("com.other.package.SomeClass");
        assertThat(cleanActivityName("com.jakewharton.test", "com.jakewharton.test.SomeClass")) //
            .isEqualTo("com.jakewharton.test.SomeClass");
        assertThat(cleanActivityName("com.jakewharton.test", "SomeClass")) //
            .isEqualTo("com.jakewharton.test.SomeClass");
        assertThat(cleanActivityName("com.jakewharton.test", ".ui.SomeClass")) //
            .isEqualTo("com.jakewharton.test.ui.SomeClass");
    }
}
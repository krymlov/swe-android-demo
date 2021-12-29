package org.swisseph;


import static swisseph.SweMini.swe_mini;

import org.junit.Test;

import swisseph.SwissEph;

public class SweMiniTest {

    @Test
    public void test_swe_mini() {
        swe_mini(new SwissEph("ephe"), 1, 1, 2022);
    }
}

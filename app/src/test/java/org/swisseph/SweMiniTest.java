package org.swisseph;


import static swisseph.SweMini.swe_mini;

import org.junit.Test;

import swisseph.SwissEph;

public class SweMiniTest {

    @Test
    public void test_swe_mini() {
        SwissEph swissEph = new SwissEph("ephe");
        swe_mini(swissEph, 1, 1, 2022);
        swissEph.swe_close();
    }
}

package com.appdynamics.extensions.redis.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by venkata.konala on 8/4/17.
 */
public class ValidityCheckerTest {
    ValidityChecker validityChecker = new ValidityChecker();

    @Test
    public void validAggregationTest(){
        Assert.assertTrue(validityChecker.validAggregation("AVERAGE"));
        Assert.assertFalse(validityChecker.validAggregation("average"));
    }

    @Test
    public void validTimeTest(){
        Assert.assertTrue(validityChecker.validTime("SUM"));
        Assert.assertFalse(validityChecker.validTime("sum"));

    }

    @Test
    public void validClusterTest(){
        Assert.assertTrue(validityChecker.validCluster("INDIVIDUAL"));
        Assert.assertFalse(validityChecker.validCluster("individual"));

    }
}

package com.appdynamics.extensions.redis.utils;

/**
 * Created by venkata.konala on 8/4/17.
 */
public class ValidityChecker {

    public boolean validAggregation(String aggregation){
        if(aggregation != null  && (aggregation.equals("AVERAGE") || aggregation.equals("SUM") || aggregation.equals("OBSERVATION"))){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean validTime(String time){
        if(time != null  && (time.equals("AVERAGE") || time.equals("SUM") || time.equals("CURRENT"))){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean validCluster(String cluster){
        if(cluster != null  && (cluster.equals("INDIVIDUAL") || cluster.equals("COLLECTIVE"))){
            return true;
        }
        else{
            return false;
        }
    }
}

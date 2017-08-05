package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.redis.utils.ValidityChecker;

import java.math.BigDecimal;

import static com.appdynamics.extensions.redis.utils.Constants.AGGREGATION_DEFAULT;
import static com.appdynamics.extensions.redis.utils.Constants.CLUSTER_ROLLUP_DEFAULT;
import static com.appdynamics.extensions.redis.utils.Constants.TIME_ROLLUP_DEFAULT;

public class MetricProperties {
    String sectionName;
    BigDecimal value;
    BigDecimal modifiedFinalValue;
    String alias;
    String multiplier;
    String aggregation;
    String time;
    String cluster;
    String delta;
    boolean isCluster;
    ValidityChecker validityChecker = new ValidityChecker();


    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public BigDecimal getInfoValue(){
        return value;
    }

    public void setValue(String value){
        if(value == null || value.equals("")){
            this.value = new BigDecimal(0);
        }
        else{
            this.value = new BigDecimal(value);
        }

    }

    public BigDecimal getModifiedFinalValue(){
        return modifiedFinalValue;
    }

    public void setModifiedFinalValue(BigDecimal modifiedFinalValue){
        this.modifiedFinalValue = modifiedFinalValue;
    }

    public String getAlias(){
        return alias;
    }

    public void setAlias(String alias, String actualMetricName){
        if(alias == null){
            this.alias = actualMetricName;
        }
        else{
            this.alias = alias;
        }
    }

    public String getMultiplier(){
        return multiplier;
    }

    public void setMultiplier(String multiplier){
        this.multiplier = multiplier;
    }

    public String getAggregation(){
        return aggregation;
    }

    public void setAggregation(String aggregation){
        if(!validityChecker.validAggregation(aggregation)){
            this.aggregation = AGGREGATION_DEFAULT;
        }
        else{
            this.aggregation = aggregation;
        }
    }

    public String getTime(){
        return time;
    }

    public void setTime(String time){
        if(!validityChecker.validTime(time)){
            this.time = TIME_ROLLUP_DEFAULT;
        }
        else{
            this.time = time;
        }
    }

    public String getCluster(){
        return cluster;
    }

    public void setCluster(String cluster){
        if(!validityChecker.validCluster(cluster)){
            this.cluster = CLUSTER_ROLLUP_DEFAULT;
        }
        else{
            this.cluster = cluster;
        }
    }

    public String getDelta() {
        return delta;
    }

    public void setDelta(String delta){
        if(delta == null){
            this.delta = "false";
        }
        else{
            this.delta = delta;
        }
    }

    public void setIsCluster(String isCluster){
        if(isCluster == null || !isCluster.equalsIgnoreCase("true")){
            this.isCluster = false;
        }
        else{
            this.isCluster = Boolean.parseBoolean(isCluster);
        }
    }

    public boolean getIsCluster(){
        return isCluster;
    }
}

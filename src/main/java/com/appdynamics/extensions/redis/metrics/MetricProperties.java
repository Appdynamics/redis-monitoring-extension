package com.appdynamics.extensions.redis.metrics;

import com.appdynamics.extensions.NumberUtils;
import com.appdynamics.extensions.redis.utils.ValidityChecker;
import com.google.common.base.Strings;
import java.math.BigDecimal;

import static com.appdynamics.extensions.redis.utils.Constants.*;

public class MetricProperties {
    String sectionName;
    BigDecimal value;
    BigDecimal modifiedFinalValue;
    String alias;
    BigDecimal multiplier;
    String aggregation;
    String time;
    String cluster;
    String delta;
    boolean aggragateAtCluster;
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

    public void setInfoValue(String value){
        if(Strings.isNullOrEmpty(value)){
            this.value = null;
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

    public BigDecimal getMultiplier(){
        return multiplier;
    }

    public void setMultiplier(String multiplier){
            BigDecimal multiplierBigD = NumberUtils.isNumber(multiplier) ? new BigDecimal(multiplier.trim()) : DEFAULT_MULTIPLIER;
            this.multiplier = multiplierBigD;
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
        if(Strings.isNullOrEmpty(delta) || !delta.equalsIgnoreCase("true")){
            this.delta = "false";
        }
        else{
            this.delta = "true";
        }
    }

    public void setAggregateAtCluster(String aggregateAtCluster){
        if(Strings.isNullOrEmpty(aggregateAtCluster) || !aggregateAtCluster.equalsIgnoreCase("true")){
            this.aggragateAtCluster = false;
        }
        else{
            this.aggragateAtCluster = true;
        }
    }

    public boolean getAggregateAtCluster(){
        return this.aggragateAtCluster;
    }
}

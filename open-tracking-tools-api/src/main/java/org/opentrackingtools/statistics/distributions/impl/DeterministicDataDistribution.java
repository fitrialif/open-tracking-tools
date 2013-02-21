package org.opentrackingtools.statistics.distributions.impl;

import java.util.Collections;
import java.util.Map;

import gov.sandia.cognition.learning.algorithm.AbstractBatchAndIncrementalLearner;
import gov.sandia.cognition.math.MutableDouble;
import gov.sandia.cognition.statistics.AbstractDataDistribution;
import gov.sandia.cognition.statistics.DataDistribution;
import gov.sandia.cognition.statistics.DistributionEstimator;
import gov.sandia.cognition.statistics.distribution.DefaultDataDistribution;

public class DeterministicDataDistribution<T> extends
    AbstractDataDistribution<T> {

  private static final long serialVersionUID = 5553981567680543038L;
  
  final protected static MutableDouble internalValue = new MutableDouble(1d);
  
  protected T element;
  
  public DeterministicDataDistribution(T element) {
    super(Collections.singletonMap(element, internalValue));
    this.element = element;
  }

  @Override
  public double getTotal() {
    return internalValue.doubleValue();
  }

  @Override
  public DistributionEstimator<T, ? extends DataDistribution<T>> getEstimator() {
    return null;
  }

  @Override
  public gov.sandia.cognition.statistics.DataDistribution.PMF<T> getProbabilityFunction() {
    return new DefaultDataDistribution.PMF<T>(this);
  }

  public static class Estimator<T>
      extends
      AbstractBatchAndIncrementalLearner<T, DefaultCountedDataDistribution.PMF<T>>
      implements
      DistributionEstimator<T, DefaultCountedDataDistribution.PMF<T>> {

    /**
       * 
       */
    private static final long serialVersionUID =
        8787720132790311008L;
    
    final private T element;

    /**
     * Default constructor
     */
    public Estimator(T element) {
      super();
      this.element = element;
    }

    @Override
    public DefaultCountedDataDistribution.PMF<T>
        createInitialLearnedObject() {
      return new DefaultCountedDataDistribution.PMF<T>(
          new DeterministicDataDistribution<T>(element), false);
    }

    /**
     * No-op: deterministic
     */
    @Override
    public
        void
        update(
          final DefaultCountedDataDistribution.PMF<T> target,
          final T data) {
    }

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((element == null) ? 0 : element.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DeterministicDataDistribution)) {
      return false;
    }
    DeterministicDataDistribution other = (DeterministicDataDistribution) obj;
    if (element == null) {
      if (other.element != null) {
        return false;
      }
    } else if (!element.equals(other.element)) {
      return false;
    }
    return true;
  }

  
}
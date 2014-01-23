package org.opentrackingtools.updater;

import gov.sandia.cognition.math.LogMath;
import gov.sandia.cognition.math.matrix.VectorFactory;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opentrackingtools.VehicleStateInitialParameters;
import org.opentrackingtools.distributions.EvaluatedPathStateDistribution;
import org.opentrackingtools.distributions.PathStateMixtureDensityModel;
import org.opentrackingtools.graph.GenericJTSGraph;
import org.opentrackingtools.graph.InferenceGraphSegment;
import org.opentrackingtools.model.GpsObservation;
import org.opentrackingtools.model.ProjectedCoordinate;
import org.opentrackingtools.model.VehicleStateDistribution;
import org.opentrackingtools.model.VehicleStateDistribution.VehicleStateDistributionFactory;
import org.opentrackingtools.paths.PathEdge;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class VehicleStatePLPathSamplingUpdaterTest {

  /**
   * Test that the prior log likelihood values are correct over edges, paths and
   * on/off states.
   */
  @Test(enabled=false)
  public void testUpdate1() {
    final List<LineString> edges = Lists.newArrayList();
    edges.add(JTSFactoryFinder.getGeometryFactory().createLineString(
        new Coordinate[] { new Coordinate(0, 0),
            new Coordinate(1, 0), }));
    edges.add(JTSFactoryFinder.getGeometryFactory().createLineString(
        new Coordinate[] { new Coordinate(1, 0),
            new Coordinate(1, 1), }));
    edges.add(JTSFactoryFinder.getGeometryFactory().createLineString(
        new Coordinate[] { new Coordinate(1, 0),
            new Coordinate(1, -1), }));
    edges.add(JTSFactoryFinder.getGeometryFactory().createLineString(
        new Coordinate[] { new Coordinate(1, 1),
            new Coordinate(1, 2), }));
    edges.add(JTSFactoryFinder.getGeometryFactory().createLineString(
        new Coordinate[] { new Coordinate(1, 2),
            new Coordinate(1, 3), }));
    final GenericJTSGraph graph = new GenericJTSGraph(edges, false);
    final InferenceGraphSegment startLine =
        Iterables.getOnlyElement(graph.getNearbyEdges(edges.get(0)
            .getCoordinate(), 0.5d));

    final Coordinate obsCoord = new Coordinate(1, 3);
    final GpsObservation obs =
        new GpsObservation("test", new Date(0l), obsCoord, null,
            null, null, 0, null, new ProjectedCoordinate(null,
                obsCoord, null));

    final Random rng = new Random(102343292l);

    final VehicleStateInitialParameters parameters =
        new VehicleStateInitialParameters(VectorFactory.getDefault()
            .copyArray(new double[] { 0d, 1d, 0d, 0d }),
            VectorFactory.getDefault().createVector2D(0.02d, 0.02d),
            Integer.MAX_VALUE, VectorFactory.getDefault()
                .createVector1D(1e-4d), Integer.MAX_VALUE,
            VectorFactory.getDefault().createVector2D(1e-4d, 1e-4d),
            Integer.MAX_VALUE, VectorFactory.getDefault()
                .createVector2D(1, Double.MAX_VALUE), VectorFactory
                .getDefault().createVector2D(Double.MAX_VALUE, 1), 0,
            4, 0);

    final PathEdge startPathEdge = new PathEdge(startLine, 0d, false);

    final VehicleStateDistributionFactory<GpsObservation, GenericJTSGraph> factory =
        new VehicleStateDistribution.VehicleStateDistributionFactory<GpsObservation, GenericJTSGraph>();
    final VehicleStateDistribution<GpsObservation> currentState =
        factory.createInitialVehicleState(parameters, graph, obs,
            rng, startPathEdge);

    final VehicleStatePLPathSamplingUpdater<GpsObservation, GenericJTSGraph> updater =
        new VehicleStatePLPathSamplingUpdater<GpsObservation, GenericJTSGraph>(
            obs, graph, factory, parameters, false, rng);

    final VehicleStateDistribution<GpsObservation> newState =
        updater.update(currentState);

    final PathStateMixtureDensityModel pathStateMixDist =
        newState.getPathStateParam().getConditionalDistribution();

    AssertJUnit
        .assertTrue(pathStateMixDist.getDistributionCount() > 1);

    /*
     * Check that our results are completely normalized.
     */
//    final double weightSum = Math.exp(pathStateMixDist.getPriorWeightSum());
//    AssertJUnit.assertEquals(1d, weightSum, 1e-5);

    /*
     * Check that on-road total prior log likelihood is equal to the off-road.  
     * (i.e. excluding edge transition considerations, on and off-road should 
     * be a priori equally likely).
     */
    double onRoadEdgeLogLikTotal = Double.NEGATIVE_INFINITY;
    double offRoadEdgeLogLikTotal = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < pathStateMixDist.getDistributionCount(); i++) {

      EvaluatedPathStateDistribution evalPathStateDist = 
          (EvaluatedPathStateDistribution) pathStateMixDist.getDistributions().get(i);
      final double logLik = evalPathStateDist.getEdgeLogLikelihood();
          //pathStateMixDist.getPriorWeights()[i];

      if (pathStateMixDist.getDistributions().get(i).getPathState()
          .isOnRoad()) {
        onRoadEdgeLogLikTotal =
            LogMath.add(onRoadEdgeLogLikTotal, logLik);
      } else {
        offRoadEdgeLogLikTotal =
            LogMath.add(offRoadEdgeLogLikTotal, logLik);
      }
    }
    AssertJUnit.assertEquals(onRoadEdgeLogLikTotal,
        offRoadEdgeLogLikTotal, 1e-5);
    AssertJUnit.assertEquals(1e-10, LogMath.add(onRoadEdgeLogLikTotal,
        offRoadEdgeLogLikTotal), 1e-5);
  }

}
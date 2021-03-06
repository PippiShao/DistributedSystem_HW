package io.swagger.client;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.api.StatisticsApi;
import io.swagger.client.model.APIStats;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

public class SkierApiMain {
  /*
  Maximum number of threads to run (numThreads - max 256)
  number of skier to generate lift rides for (numSkiers - max 50000), This is effectively the skier’s ID (skierID)
  number of ski lifts (numLifts - range 5-60, default 40)
  mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
  IP/port address of the server
  */
  static final int numThreads = 256;
  static final int numSkiers = 20000;
  static final int numLifts = 40;
  static final int numRuns = 20;

  static final boolean enableBlockingQueue = true;

  // Each phase should start this percent of maxThreads
  static final double warmup = 0.25;
  static final double peak = 1;
  static final double cooldown = 0.25;

  // Each phase should start this percent of requests
  // It will POST request (numRunsx0.8)x(numSkiers/numThreads) times
  static final double warmupRequest = 0.1;
  static final double peakRequest = 0.8;
  static final double cooldownRequest = 0.1;

  // time range for each phase
  static final int[] warmupTimeRange = new int[]{1, 91};
  static final int[] peakTimeRange = new int[]{91, 361};
  static final int[] cooldownTimeRange = new int[]{361, 421};

  // Once this percent of threads in current phase has completed,
  // next phase should begin.
  static final double percentBeforeNextPhase = 0.1;

  // BasePath URL
//  static final String localBasePath = "http://localhost:8080/CS6650_war";
//  static final String localBasePath = "http://ec2-54-214-219-231.us-west-2.compute.amazonaws.com:8080/CS6650_war";
//  static final String localBasePath = "http://ec2-52-38-143-124.us-west-2.compute.amazonaws.com:8080/CS6650_war";
//  static final String localBasePath = "http://ec2-34-220-160-52.us-west-2.compute.amazonaws.com:8080/CS6650_war";
//  static final String localBasePath = "http://ec2-54-188-158-1.us-west-2.compute.amazonaws.com:8080/CS6650_war";
  static final String localBasePath = "http://CS6650-fc8726c3eeedc97c.elb.us-west-2.amazonaws.com:8080/CS6650_war"; // load balancer

//  static final String localBasePath = "http://ec2-52-88-117-225.us-west-2.compute.amazonaws.com:8080/app-server_war"; // Mao
//  static final String localBasePath = "http://34.217.208.47:8080/SkiResorts_war"; // Yun Wu
//  static final String localBasePath = "http://54.191.39.74:8080/server"; //shangzhen

  public static int numRequestSuccessfulPost = 0;
  public static int numRequestFailPost = 0;

  public static int numRequestSuccessfulGet = 0;
  public static int numRequestFailGet = 0;

  public static void main(String[] args)
      throws BrokenBarrierException, ApiException, InterruptedException {

    // Mark start time
    long startTime = System.currentTimeMillis();

    // Calculate threads for each phase
    int threadsNumCurrent = (int) Math.ceil(numThreads * warmup);  // Number of threads need to start for current phase
    int threadsNumBeforeNext = (int) Math.ceil(percentBeforeNextPhase * threadsNumCurrent);  // Number of threads need to complete before starting next phase
    int threadsNumCurrent2 = (int) Math.ceil(numThreads * peak);
    int threadsNumBeforeNext2 = (int) Math.ceil(percentBeforeNextPhase * threadsNumCurrent);
    int threadsNumCurrent3 = (int) Math.ceil(numThreads * cooldown);
    int threadsNumBeforeNext3 = threadsNumCurrent3;

    BlockingQueue<RequestData> blockingQueuePost = new LinkedBlockingDeque<>();
    BlockingQueue<RequestData> blockingQueueGet = new LinkedBlockingDeque<>();


    CountDownLatch countDownLatchOverall = new CountDownLatch(
        threadsNumCurrent
        + threadsNumCurrent2
        + threadsNumCurrent3);

    // phase 1=============================================================================
    System.out.println("Start Phase 1");
    CountDownLatch countDownLatch = new CountDownLatch(threadsNumBeforeNext);
    Phase.Phase(countDownLatchOverall, countDownLatch, threadsNumCurrent,
        localBasePath, numRuns, numSkiers, warmupRequest, warmup,
        blockingQueuePost, blockingQueueGet, warmupTimeRange);
    countDownLatch.await();
    System.out.println("Terminating Phase 1...");

    // phase 2=============================================================================
    System.out.println("Start Phase 2");
    CountDownLatch countDownLatch2 = new CountDownLatch(threadsNumBeforeNext2);
    Phase.Phase(countDownLatchOverall, countDownLatch2,threadsNumCurrent2,
        localBasePath, numRuns, numSkiers, peakRequest, peak,
        blockingQueuePost, blockingQueueGet, peakTimeRange);
    countDownLatch2.await();
    System.out.println("Terminating Phase 2...");

    // phase 3=============================================================================
    System.out.println("Start Phase 3");
    CountDownLatch countDownLatch3 = new CountDownLatch(1);
    Phase.Phase(countDownLatchOverall, countDownLatch3, threadsNumCurrent3,
        localBasePath, numRuns, numSkiers, cooldownRequest, cooldown,
        blockingQueuePost, blockingQueueGet, cooldownTimeRange);
    countDownLatch3.await();
    System.out.println("Terminating Phase 3...");

    // Wait till all phase to finish
    countDownLatchOverall.await();

    // Mark end time and calculate for wall time
    long endTime = System.currentTimeMillis();
    long wallTime = endTime-startTime;

    // Start Output on the Screen===========================================================================
    System.out.println("Logistics --------------------------------");
    System.out.println("numThreads = " + numThreads);
    System.out.println("numSkiers = " + numSkiers);
    System.out.println("numLifts = " + numLifts);
    System.out.println("numRuns = " + numRuns);
    System.out.println("wall time for all phase = " + wallTime + " millisec");
    System.out.println("numPostRequestSuccessful = " + numRequestSuccessfulPost);
    System.out.println("numPostRequestFail = " + numRequestFailPost);
    System.out.println("numGetRequestSuccessful = " + numRequestSuccessfulGet);
    System.out.println("numGetRequestFail = " + numRequestFailGet);
    // End Output on the Screen=============================================================================


    if (!enableBlockingQueue) return;

    // Write results from blockingQueue to CSV file=========================================================
    String filePath = "output"+"numThreads"+numThreads+"_"+"numSkiers"+numSkiers+"_"+"numRuns"+numRuns+".csv";
    BlockingQueueToCSV.writeToCSV(filePath, blockingQueuePost);
    // =====================================================================================================

    // process data and generate plot=======================================================================
    System.out.println("statistics");
    System.out.println("Post requests:-------------------------------");
    GenerateStatistics.generateStatistics(blockingQueuePost, numRequestSuccessfulPost, numRequestFailPost, wallTime);
    System.out.println("Get requests:--------------------------------");
    GenerateStatistics.generateStatistics(blockingQueueGet, numRequestSuccessfulGet, numRequestFailGet, wallTime);
    System.out.println("Throughput: ---------------------------------");
    double throughput = (double) (numRequestSuccessfulPost + numRequestSuccessfulGet +
            numRequestFailPost + numRequestFailGet) / wallTime * 1000;
    System.out.println("Throughput = " + throughput);

    // =====================================================================================================
    StatisticsApi apiInstance = new StatisticsApi();
    ApiClient client = apiInstance.getApiClient();
    client.setBasePath(localBasePath);

    APIStats apiResponse = apiInstance.getPerformanceStats();
    System.out.println(apiResponse.toString());
  }
}

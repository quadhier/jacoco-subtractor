import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;

public class JacocoExecSubtractor {
  public static void main(String[] args) throws IOException {
    if (args.length != 2 && args.length != 3) {
      System.out.println("Usage: jacocosub <execFile1> <execFile2> [<resultExecFile>]");
      return;
    }

    File execFile1 = new File(args[0]);
    File execFile2 = new File(args[1]);
    File resultFile = args.length == 3 ? new File(args[2]) : new File("subtract.exec");

    // Load both exec files
    ExecFileLoader loader1 = new ExecFileLoader();
    loader1.load(execFile1);

    ExecFileLoader loader2 = new ExecFileLoader();
    loader2.load(execFile2);

    // This will store the resulting execution data
    ExecutionDataStore resultStore = new ExecutionDataStore();

    // Subtract execution data from loader2 from loader1
    loader1.getExecutionDataStore().getContents().forEach(data1 -> {
      ExecutionData data2 = loader2.getExecutionDataStore().get(data1.getId());
      if (data2 != null) {
        // Clear coverage in data1 that is also covered in data2
        boolean[] data1Probes = data1.getProbes();
        boolean[] data2Probes = data2.getProbes();
        if (data1Probes.length != data2Probes.length) {
          throw new RuntimeException(String.format("Different probe length for %s (%d) and %s (%d)",
              data1.getName(), data1Probes.length, data2, data2Probes.length));
        }
        for (int i = 0; i < data1Probes.length; i++) {
          // If a line is covered in both data1 and data2, clear it in data1
          if (data1Probes[i] && data2Probes[i]) {
            data1Probes[i] = false;
          }
        }
      }
      // Add modified data1 to the result store
      resultStore.put(data1);
    });

    // Write the resulting execution data to a new exec file
    try {
      loader1.save(resultFile, false);
    } catch (IOException e) {
      System.err.println("Error writing the subtracted exec file: " + e.getMessage());
      e.printStackTrace();
    }
  }
}

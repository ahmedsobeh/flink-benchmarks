/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.benchmark.checkpointing;


import org.apache.flink.benchmark.BenchmarkBase;
import org.openjdk.jmh.runner.RunnerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;



/**
 * Creates a kubernetes FLink cluster and submits a selected job to it, with checkpointing
 * either enabled or disabled. Next, prints the number of requests to the
 * Kubernetes API server.
 *
 * The only requirements for running are Docker and minikube.
 */
public class KubernetesCheckpointingBenchmark extends BenchmarkBase {
	public static void main(String[] args) throws RunnerException, IOException {
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("./src/main/java/org/apache/flink/benchmark/checkpointing/setup.sh");

			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String line;

			while((line=input.readLine()) != null) {
				System.out.println(line);
			}

			int exitVal = pr.waitFor();
			System.out.println("Exited with error code "+exitVal);
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			System.out.println("Enter 1 to run with checkpointing, 2 without: ");
			int n = reader.nextInt();
			if (n == 1) {
				createPod(1);
			} else if (n == 2) {
				createPod(2);
			}
			reader.close();


		} catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public static void createPod(int checkpointing) {
		try {
			String cmd= "./src/main/java/org/apache/flink/benchmark/checkpointing/run.sh " + checkpointing ;
			ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
			Process pr = pb.start();

			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String line;

			while((line=input.readLine()) != null) {
				System.out.println(line);
			}

			int exitVal = pr.waitFor();
			System.out.println("Exited with error code "+exitVal);
		} catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
}

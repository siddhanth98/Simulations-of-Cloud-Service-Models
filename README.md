## Cloud Datacenter Simulations
This project constructs different cloud datacenters having customizable configurations 
load balancing policies, and implements a datacenter broker which accepts user specified task configurations
to allocate those tasks in the form of cloudlets using different VM/CPU allocation and scheduling policies.

### Running Simulations
***
   * All programs are in the `src/main/java/cloudsimplus/*` packages.
   * To run a program like simulation 2 of datacenter 2 use the following sbt command:
    - sbt runMain cloudsimplus.datacenter2.simulations.simulation2.MainSimulation cloudlets
   * All logs, outputs and configuration files are located in the src/main/resources/configuration/*
   directories and are accessed by the programs for inputs and in this file as references to logs.
   * All Junit Tests are located in the src/test/* packages for respective simulations.
   
   * Snippets of outputs are used in this documentation for clarity of explanation but can be verified by
   running the appropriate programs or opening images in the resources/outputs directory using the highlighted paths. 
   
**For all simulations of datacenters 1 and 2 (homework parts 1 to 4) passing different command line arguments while running the programs can produce different
results. 
You can also combine arguments to get all results together in the output.**

* To print cloudlets execution results, run as: runMain filename cloudlets
* To print Host utilization metrics, run as: runMain filename hosts
* To print VM utilization metrics, run as: runMain filename vms

**For example** - runMain cloudsimplus.datacenter1.simulations.simulation5.MainSimulation cloudlets

**All logs are placed in the src/main/resources/logs/ directory**
 
### Datacenter 1 (10 hosts, datacenterbroker simple, network data center, cloudlet simple):
***
* Simulations 1 and 2 compare results of the Best Fit and Worst Fit Allocation Policies used to map VMs to hosts.
* VM Allocation best fit and worst fit policies give similar cloudlet execution results for this configuration
    with slightly different placement of VMs.

* For simulation 1, best fit will fill the 1st 5 hosts fully(all PEs in use) with 10 VMs in total(2 VMs per host).
	Remaining 5 hosts will be idle. While the worst fit will partially fill(half of the PEs in use) 
	all 10 hosts with 10 VMs in total (1 VM per host).
	
* For simulation 2, best fit and worst fit have the exact same placement of VMs in hosts as there is just 1 VM per
	host using all of the host's PEs.

* For simulation 2, best fit and worst fit have the exact same placement of VMs in hosts as there is just 1 VM per
  	host using all of the host's PEs. This can be viewed in the following log files for programs using best fit and worst fit policies:
  	 
  	 src/main/resources/logs/datacenter 1/simulation2_MainSimulation1.log
  	 src/main/resources/logs/datacenter 1/simulation2_MainSimulation2.log
  	 
* Even if extra VMs need to be allocated in any case then idle hosts are selected for new VMs in best fit policy
  	and all currently running 10 hosts with idle resources in the worst fit policy.
  	
* Dynamic stream of cloudlets is simulated using additional 20 cloudlets with 100 seconds delays.
  	For both simulations every cloudlet uses the stochastic/random utilization model to use VM resources
  	and every VM uses the space shared scheduling policy for its cloudlets.
 
    ### Simulation 1
    * VM Allocation Best Fit for 10 VMs and 5 PEs per VM.
    
    src/main/resources/outputs/datacenter 1/simulation1_main_simulation1.jpg
    
    ![Datacenter 1 - Simulation 1](src/main/resources/outputs/datacenter 1/simulation1_main_simulation1.jpg)
   
   * Cloudlets 0 - 19 instantly start executing in their respective VMs without any delays.
        Every host has 2 VMs and every VM has 2 cloudlets executing in order of IDs using a space shared manner.
        That is, in VM0 in host0 for instance, cloudlet 0 and cloudlet 10 execute. 
        Cloudlet 0 executes fully before cloudlet 10 starts as it uses all of the 5 PEs of VM0.
        So cloudlet 10 starts executing after 172 simulation seconds which is cloudlet 0's end simulation time.
   * After 100 seconds, cloudlets 20 to 39 are mapped to the respective VMs in order of IDs.
     		So cloudlet 20 and 30 will be mapped to VM0 in host0 as VM0 has enough PEs to execute.
     		But just like before cloudlet 20 waits for cloudlet 10 to execute fully and after 320 simulation seconds,
     		starts executing. Then after 20 completes 30 will start executing. This execution pattern is consistent 
     		across all VMs in all hosts for this simulation.
   * The above timings are for 1 run of the program. A different run may give slightly different timings but similar.
   
   
### Simulation 2
   * VM allocation best fit for 10VMs and 10 PEs per VM.
   
   src/main/resources/outputs/datacenter 1/simulation1_main_simulation2.jpg
   
   ![Datacenter 1 - Simulation 2](src/main/resources/outputs/datacenter 1/simulation1_main_simulation2.jpg)
   
   * For this configuration, there is just 1 VM per host using 10 PEs and 2 cloudlets execute at the same time
   		in a given VM as there is enough PEs for both of them to execute. This is clear from the above cloudlet results
   		table printed obtained the output, where the 1st 20 cloudlets start after 0 simulation seconds.
   		
  
### Differences
  * The important differences to note in the above 2 simulations are as follows.
    * For **simulation 1**:
        As the number of PEs used per VM is less in this case(5 PEs/VM), a lot of cloudlets have to wait 
        to even start executing after being submitted by the broker. For tasks which are time constrained,
        this might not work well and the cloudlets will simply fail without executing.
        			 
    * For **simulation 2**:
        The number of PEs per VM is more than that in simulation 1 (10 PEs/VM). So all the undelayed cloudlets
        start executing at the same time without having to wait, but fight for contention of other resources. 
        As a random/stochastic utilization model is used the RAM/BW usage by a cloudlet may vary significantly 
        across different time intervals and the required amount may not be available immediately, as is clear 
        from logs.
        Notice that this was not a problem in the 1st simulation as there was just 1 cloudlet executing
        at any given time in a VM.
        Still the execution time per cloudlet does not vary much for both simulations as scheduling policy used
        is the same and there are enough PEs available for all cloudlets executing in a VM.
       
* In simulation 3 and 4, different cloudlet scheduling policies are used for execution in order to observe
    differences in cloudlet start and execution times.

###Simulation 3 (Space shared Cloudlet Scheduling Policy)
* For this simulation, 10 VMs are allocated to the 1st 5 hosts using the best fit allocation policy.
* Each VM uses 5 PEs and a space shared policy for scheduling its cloudlets, indicating that only 1 cloudlet
		can use a given PE at any time and any other cloudlet can use the same PE once the first one completely
		executes. The last 20 cloudlets (cloudlet 20 to 39) are submitted with a delay of 100 seconds.
		The configuration for this is same as that for simulation 1.
  
  src/main/resources/outputs/datacenter 1/simulation3_main_simulation.jpg
  
  ![Datacenter 1 - Simulation 3](src/main/resources/outputs/datacenter 1/simulation3_main_simulation.jpg)
* From the simulation results table, it can be observed that the 1st 10 cloudlets start executing immediately
  		after being submitted, while the successive cloudlets start executing later on.
  		This waiting time for cloudlets 10-39 is indicative of a space shared scheduling policy being used.
  		
* However the overall execution time for each cloudlet is roughly the same as it would be had it been the
only cloudlet mapped to that VM. Notice that will not be the case if our configuration is something like
in simulation 2 where multiple cloudlets execute in the same VM in a host due to enough availability of 
resources.

###Simulation 4 (Time shared cloudlet scheduling policy)
* Configuration is same as in simulation 3 with the only difference being the cloudlet scheduling policy 
  		used for this one which is the time shared policy.
  		All cloudlets submitted at the same time start executing together.
  
* If there are enough PEs in a VM for 2 cloudlets then this policy will allocate separate time slices for
both cloudlets to execute. 

    src/main/resources/outputs/datacenter 1/simulation4_main_simulation.jpg
    
    ![Datacenter 1 - Simulation 4](src/main/resources/outputs/datacenter 1/simulation4_main_simulation.jpg)
    
* For instance in this simulation, initially for the 1st 100 seconds, VM 0 will keep on switching 
    from cloudlet 0 to cloudlet 10 giving equal time slices of execution to each of them. 
    After 100 seconds when the additional 20 cloudlets have been submitted and mapped by the broker, 
    cloudlet 20 and 30 join VM 0. So the execution time is divided among these 4 cloudlets currently 
    fighting for contention of VM 0 resources.
    
* The overall execution time per cloudlet increases significantly because of this, though the waiting time
    for each (delayed only/undelayed only) cloudlet to start execution is the same. 
    We can compare the execution times for cloudlets from simulation 3 where the highest was about 
    315 simulation seconds and simulation 4 where the highest execution time was around 905 simulation seconds.

* In order to reduce the execution time per cloudlet in this time shared scenario, we would need to reduce
    the number of cloudlets mapped to the same VM and that is exactly what simulation 5 shows in its result table.
    
    
### Simulation 5
    
   src/main/resources/outputs/datacenter 1/simulation5_main_simulation.jpg
   
   ![Datacenter 1 - Simulation 5](src/main/resources/outputs/datacenter 1/simulation5_main_simulation.jpg)
   
   * This is similar to simulation 4 except that there are no additional cloudlet being submitted to the 
     broker with submission delays.
   * The execution time per cloudlet reduces in this simulation as there are less number of cloudlets fighting
        for utilization of resources.
        
### Datacenter 2 (50 network hosts, 2 edge switches, 1 aggregate switch, 20 seconds scheduling interval):
***
* Simulations for this datacenter show differences in dynamic network cloudlet executions when VMs are
    being horizontally scaled using a specific overload condition and load balanced vs when load balancing is not
  	used. 
  	
* Both simulations use the best fit VM Allocation policy, time shared VM scheduling policy and space 
  	shared cloudlet scheduling policy and simulate a stream of cloudlets at every alternate time interval.
  	
* If "cloudlets" is passed as a cmd argument then 2 result tables are created for each simulation.
  	The 1st table is sorted by cloudlet ID and the 2nd is sorted by execution start time to highlight differences 
  	between both simulation results.
  
* If "vms" is passed as a cmd argument then the utilization metrics of all VMs which have executed are printed.
  	To reduce log size, the metrics are collected once every 3 simulation intervals.
  
* The stream of cloudlets stop when 100 simulation seconds pass.

    ### Simulation 1
    `src/main/resources/outputs/datacenter 2/simulation1_cloudlets_results_start.jpg`
    
    ![Datacenter 2 - Simulation 1](src/main/resources/outputs/datacenter 2/simulation1_cloudlets_results_start.jpg)
    
    `src/main/resources/outputs/datacenter 2/simulation1_cloudlets_results.jpg`
    
    ![Datacenter 2 - Simulation 1](src/main/resources/outputs/datacenter 2/simulation1_cloudlets_results.jpg)
    
    `src/main/resources/outputs/datacenter 2/simulation1_cloudlets_execution_start_time_sorted_results_start.jpg`
    
    ![Datacenter 2 - Simulation 1](src/main/resources/outputs/datacenter 2/simulation1_cloudlets_execution_start_time_sorted_results_start.jpg)
    
    src/main/resources/outputs/datacenter 2/simulation1_cloudlets_execution_start_time_sorted_results.jpg
    
    ![Datacenter 2 - Simulation 1](src/main/resources/outputs/datacenter 2/simulation1_cloudlets_execution_start_time_sorted_results.jpg)
    
    * The above table shows the last parts of both cloudlets results table for this simulation. Note that execution/start/end/ 
        times may slightly vary for different runs of the program.
          
    * Observing different runs of this simulation for the above mentioned configuration with 25 cloudlets being
        created every alternate simulation interval, the total number of cloudlets created and which finished execution
        is around 134 from the 1st table, and the latest that a cloudlet started execution is approximately after
        2800 simulation seconds from the start of the simulation.
    
    * As for the VM utilization metrics, every run will give slightly different execution results as the cloudlets
    use a random / stochastic utilization model for VM resources.
      
    ### Simulation 2 (note that here the broker uses a delay of 10 seconds to destroy idle VMs)
    * This simulation uses a horizontal scaling policy for each VM, specifically when the cpu utilization of
        the VM exceeds 70% of the total available MIPS. When this happens one additional VM is created and submitted
        to the broker.
        
    src/main/resources/outputs/datacenter 2/simulation2_cloudlets_results.jpg
    
    ![Datacenter 2 - Simulation 2](src/main/resources/outputs/datacenter 2/simulation2_cloudlets_results.jpg)
    
    * For this simulation, from the cloudlet results of different runs, the total number of cloudlets created
        and which finished executing ranges from around 180 to 230. This variation may be because of the different
        times at which requests for additional VMs maybe submitted as a result of the random utilization of 
        already existing VM	resources by executing cloudlets.
  
    * However for the VM utilization results, each VM tends to execute all its cloudlets much sooner than it did
        in simulation 1. This is apparent from each VMs utilization results close to the end of the simulation when
        all resource utilization becomes 0.00%.
        
    * For instance for one of the runs in both simulations as shown below: 
        src/main/resources/outputs/datacenter 2/simulation1_2_vms_results_comparison.jpg
        
        ![Datacenter 2 - Simulation 2](src/main/resources/outputs/datacenter 2/simulation1_2_vms_results_comparison.jpg)
        
        The left table is the vm utilization results for simulation 1 and the right table is the vm utilization results for simulation 2.
        VM 5 in simulation 1 became idle after 2886.44 simulation seconds when its CPU utilization reached 0%.
        VM 5 in simulation 2 became idle after 2340.24 simulation seconds when its CPU utilization reached 0%.
        
        Similarly, VM 2 in simulation 1 became idle after 2705.07 simulation seconds and in simulation 2 it became idle
        after 2464.73 simulation seconds.
        
    * Notice that in the above tables, the RAM and BW stay allocated even after VM 5 becomes idle in simulation1,
      while for simulation 2, RAM and BW utilizations become 0.0% at around the same time the CPU utilization becomes 0.0%.
      This is due to the fact that the broker in simulation 2 is set to destroy idle VMs after 10.0 simulation seconds, while
      in simulation 1 the VMs stay idle till the end of the simulation. This can be hugely beneficial in some cases as the host
      resources become free for other VM configurations to be created inside it, for instance, if another waiting cloudlet requires
      a VM of higher specications to be created in some host.
      
   
   * The above differences can be explained as follows:
        1. **Cloudlet execution**:
            All cloudlets executing use all of the PEs of their respective VMs so only 1 cloudlet 
            can execute at a time.
 
            Initially submitted cloudlets will start executing in their VMs immediately in both simulations as
            expected, and this is clear from the 2nd table results for the 1st 10 cloudlets of both simulations.
            
            However as more and more cloudlets start arriving in the 1st simulation they are inserted in a waiting
            queue by the broker as no more VMs maybe available, so the execution start time of these new cloudlets
            depends on the execution time of the already executing cloudlets.
 
            For the 2nd simulation, as the initial cloudlets utilize PEs there will be a point where this will go
            above 70% and the scaling policy will start creating additional VMs in other hosts where resources 
            are available. As the datacenter broker used for this maps a cloudlet to the 1st VM which has 
            enough resources it will map the newly arrived cloudlets to these new VMs (assuming these VMs exist at that time), 
            where they will start executing immediately. If VMs are not available then they are inserted in waiting
            queues.
            This is clear from the execution start times of these new cloudlets which is equal to the time interval
            at which they are created and the VM IDs in which these new cloudlets execute which correspond to the 
            new VMs.
            By the time the next batch of new cloudlets arrive the previous VMs may have finished executing
            (all cloudlets in execList and waitingList) and will take in these new cloudlets.
            
            This explains why the number of cloudlets served in the 2nd simulation is higher than that in the 1st
            simulation.
        
        2. **VM utilization**:
            Relating to the previous explanation every VM in the 2nd simulation will tend to have fewer cloudlets
            in its waiting queue than it did in the 1st simulation because of load balancing and thus will tend
            to finish executing all its cloudlets quickly, and then become idle. The broker will then destroy 
            if these VMs stay idle for more than 10 simulation seconds.
            This is explained by the quick drop to 0% CPU, RAM and BW usage in simulation 2. 
            
            
### Part 5 (Comparing SaaS and IaaS execution and datacenter pricing criteria):
***
   * This part implements 3 datacenters with different configurations and pricing criteria to utilize their resources.
   The configurations for each datacenter are given below.
   
   * The main goal of this simulation is to determine whether the broker is over/under estimating the cloudlet execution costs
   and comparing the estimated and actual execution costs of the cloudlet in the selected datacenter.
   (Note that the cloudlet's utilization of VM resources is random/stochastic)
    
   * SaaS services are simulated as file operations namely "Process File" and "Open file" on files "File1", "File2" and "File3.
    These are specified in configurations files named services.conf in the respective datacenter directories. 
    The process operation is considered to be compute and RAM intensive so datacenters are programmed to allocate VMs with bigger RAMs and PE 
    configurations for cloudlets requesting these operations.
    
   * All datacenters support different types of operations on subsets of files. For instance, datacenter 1 supports
   processing file1 and opening file2 and file3, datacenter 2 supports opening and processing file1 and opening file2.
   
   * **Configurations of datacenter 1**
        * 100 hosts
        * 10 edge switches
        * $0.25 for using each PE per second
        * $0.25 for using 1MB of Ram
        * $0.25 for using 1Mb of Bandwidth
        * $0.25 for using 1MB of storage
   
   * **Configurations of datacenter 2**
       * 100 hosts
       * 10 edge switches
       * $0.5 for using each PE per second
       * $0.3 for using 1MB of Ram
       * $0.3 for using 1Mb of Bandwidth
       * $0.4 for using 1MB of storage
    
   * **Configurations of datacenter 3**
        * 80 hosts
        * 8 edge switches
        * $0.2 for using each PE per second
        * $0.2 for using 1MB of Ram
        * $0.5 for using 1Mb of Bandwidth
        * $0.25 for using 1MB of storage
        
   * The main broker is implemented to accept cloudlet specifications, estimate cloudlet execution costs/usages for the resources
   specifically Cpu and BW which are used to compute the total execution costs (both for estimated ones and actual ones in cloudsimplus)
   and send the cloudlet to one of the datacenters depending on the costs.
   
   * The estimated costs for executing in a datacenter are computed as follows:
        
    Total CPU execution cost of a cloudlet = (Cost of using 1 PE per second in the datacenter x Number of PEs requested by the cloudlet) x (Length of cloudlet execution / MIPS rating offered by datacenter)
    Total Bandwidth cost of a cloudlet = (Cost of using 1 Mb of Bandwidth in the datacenter x (Size of Input file size + Size of output file size))
    Total execution cost of the cloudlet = Total CPU execution cost + Total Bandwidth cost
   
   * The broker will select the datacenter which provides the file service requested by the cloudlet and at the same time 
   yielding the cheapest cost for executing the given cloudlet and submit it to the actual broker instance in the datacenter.
   * If none of the datacenters provide the service requested then the cloudlet will not be sent to any of them.
   
   * Each datacenter uses its own cloud simulation instance to prevent the broker from sending cloudlets to another datacenter's VMs
   as implemented by the default broker mapping policy. 
   
   * The simulations will start together when all cloudlets have been mapped to some datacenter.
   The results will be output in sequence for each datacenter.
   
   ### SaaS Simulation
   * For the SaaS simulation the datacenter is programmed to select its own VM specifications for incoming cloudlets
   
        src/main/resources/outputs/part 5/saas_results_1.jpg
        
        ![SaaS Simulation](src/main/resources/outputs/part 5/saas_results_1.jpg)
   
   * The above image shows results for 1 run for 2 of the cloudlets executing in datacenter 2.
   * There is a significant difference in the estimated and actual CPU execution times and the RAM / Bandwidth consumption 
   by each cloudlet.
   * However the overall estimated and actual execution costs remain around the same because of the compensation of overall
    resource usage. For instance, cloudlet 17 is estimated to execute for 0.1 seconds or 10 milliseconds based on its length
    and MIPS value offered, but it actually executed for 1 second (10 times the estimated value), whereas for the bandwidth,
    the cloudlet is estimated to use 210 Mb of bandwidth but it ends up using just around 41 Mb of bandwidth.
    * So the total cost is compensated due to these different variations in the estimated and actual resource usages.
    * These random variations in the utilizations are due to the following:
        * Stochastic utilization of resources
        * Scheduling policies and VM specs which are unknown to the broker and will be offered by the datacenter.
        So if actual broker instance decided to place the cloudlet in the VM in the same host in which the data/file required resides 
        then the actual bandwidth consumed will be lower as getting a file from the same host costs less than getting a file
        from a remote host due bandwidth resources being shared among multiple cloudlets, if more than 1 cloudlet is executing in the
        VM at the same time.
   
   ### IaaS simulation
   * This simulation is similar to the SaaS simulation except that the broker accepts both cloudlet as well as VM specifications
   from the main simulation program.
   * The broker uses the same criteria for estimating execution costs, except that the MIPS value considered for computing 
   CPU execution cost is the actual MIPS value which will be offered by the datacenter as it is the consumer who asks for 
   specific VM resources to be allocated to its cloudlet.
   * However this simulation tends to shut down the broker instance immediately after submitting the cloudlets to the VMs,
    destroying all VMs in the process as seen in the following log:
           `src/main/resources/logs/part 5/IaaS_Simulation.log`              
    
   * Thus the cloudlets are unable to finish their execution.
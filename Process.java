class Process {
	
	int id;
	int arrivalTime; //A
	int cpuBase; //B
	int cpuTime; //C
	int ioBase; //IO
	int burst;
	int remainingRun; //to keep track when preempted
	int remainingCPUBurst;
	int finishTime;
	int turnTime;
	int ioTime;
	int waitTime;
	String status;
	int quantum;
	int priority;
	

	public Process(int A, int B, int C, int IO){
		this.arrivalTime = A;
		this.cpuBase = B;
		this.cpuTime = C;
		this.remainingCPUBurst = C;
		this.remainingRun = 0;
		this.ioBase = IO;
		this.status = "unstarted";
		this.quantum = 2;
	}
}
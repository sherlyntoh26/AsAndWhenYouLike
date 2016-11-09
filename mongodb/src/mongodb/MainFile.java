

public class MainFile {
	private String dbName;
	private int dbNo;
	private String ipAdd;
	private int transactionFileNumber;

	public static void main(String[] args) throws InterruptedException {
		int dbNo;
		String dbName;
		int noOfNodes;
		int transactionFileNumber;
		if (args == null || args.length <= 0) {
			dbNo = 8;
			dbName = "D8data";
			noOfNodes = 1;
			transactionFileNumber = 0;
		} else {
			boolean usingD8 = (args[0].trim()).equals("D8data");
			if(usingD8){
				dbName = "D8data";
				dbNo = 8;
			}else{
				dbName = "D40data";
				dbNo = 40;
			}
			noOfNodes = Integer.parseInt(args[1]);
			transactionFileNumber = Integer.parseInt(args[2]);
		}
		
		Connection[] connections = new Connection[3];
		connections[0] = new Connection();
		connections[0].connect("192.168.48.249", dbName);
		if(noOfNodes==3){
			connections[1] = new Connection();
			connections[1].connect("192.168.48.250", dbName);
			connections[2] = new Connection();
			connections[2].connect("192.168.48.251", dbName);
		}
		ClientThread[] clientThreads = new ClientThread[transactionFileNumber];
		int noNum = 0;
		try{
			for(int i = 0;i<transactionFileNumber ; i++){
				System.out.println("Processing file "+i+".txt");
				String threadName = "Thread"+i;
				clientThreads[i] = new ClientThread(dbNo, connections[noNum], i, threadName);
				clientThreads[i].start();
				noNum++;
				if(noNum == noOfNodes){
					noNum = 0;
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		for (ClientThread thread : clientThreads)
		{
			System.out.println("I am here : "+thread.threadName);
		    thread.joinThread();
		}
		
		connections[0].close();
		if(noOfNodes == 3){
			connections[1].close();
			connections[2].close();
		}
		
		float max = -1;
		float min = 99999;
		float average, total = 0;
	
		for (ClientThread thread : clientThreads) {
		    if(thread.throughput > max){
		    	max = thread.throughput;
		    }
		    if(thread.throughput < min){
		    	min = thread.throughput;
		    }
		    total += thread.throughput;
		}
		
		average = total/transactionFileNumber;
		System.out.println(String.format("Maximum Throughput: %.02f", max));
		System.out.println(String.format("Minimum Throughput: %.02f", min));
		System.out.println(String.format("Average Throughput: %.02f", average));
	}
	
	public MainFile(int dbNo, String dbName, String ipAdd, int transactionFileNumber){
		this.dbNo = dbNo;
		this.ipAdd = ipAdd;
		this.dbName = dbName;
		this.transactionFileNumber = transactionFileNumber;
	}
}

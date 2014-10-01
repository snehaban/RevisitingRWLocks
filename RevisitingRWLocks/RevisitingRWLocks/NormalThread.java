package cse505_hw2;

/**
 * @Title   CSE505 Assignment2 - Revisiting RW Locks
 * @Name    Sneha Banerjee
 * @UBemail snehaban@buffalo.edu
 */

public class NormalThread extends Thread {
    
    CDLList<String> cdl;
    int id;
    CDLList<String>.Cursor cursor;
    public NormalThread(CDLList<String> cdl, int id) {
        this.id = id;
        this.cdl = cdl;
        cursor = cdl.reader(cdl.head());
    }

    @Override
    public void run() 
    {

        int offset = id * 2;
        for(int i = 0; i < offset; i++) {
            cursor.next();
        }
        try
        {
        	cursor.writer().insertBefore("IB - " + id);
        	cursor.writer().insertAfter("IA - " + id);  
        }
		catch(Exception e)
		{
			System.out.print(e.getMessage());
		}
    }

}

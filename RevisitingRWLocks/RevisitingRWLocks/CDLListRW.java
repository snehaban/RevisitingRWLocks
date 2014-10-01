package cse505_hw2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Title   CSE505 Assignment2 - Revisiting RW Locks
 * @Name    Sneha Banerjee
 * @UBemail snehaban@buffalo.edu
 */

public class CDLListRW 
{
	AtomicInteger count;
	
	public CDLListRW()
	{
		count = new AtomicInteger(1);
	}
       
    public CDLListRW lockRead() throws InterruptedException
    {
    	while(true)
		{
			if(count.get() > 0) // either unlocked (n=1) or no writers enqueued
			{
				synchronized(this)
				{
					int rdr = count.get();
					if(rdr > 0)
					{						
						count.compareAndSet(rdr, rdr+1);
						//System.out.println("\n"+Thread.currentThread()+"Reader locked - "+count.get());
						return this;
					}
				}
			}
			else
			{
				synchronized(this)
				{
					try 
					{
						if(count.get() < -1) 
						{
							wait(); 
						} 
					}
					catch (InterruptedException e) 	{ }
				}
			}
					
		} // end while
    }
   
    public void unlockRead()
    {
    	while(true)
		{
			if(count.get() > 1) // only readers have locked
			{
				synchronized(this)
				{
					int rdr = count.get();
					if(rdr > 1) 
					{
						count.compareAndSet(rdr, rdr-1);						
						//System.out.println("\n"+Thread.currentThread()+"Reader Unlocked - "+count.get());
						notifyAll();
						return;
					}
				}
			}
			else if(count.get() < -1) // writer enqueued
			{
				synchronized(this)
				{
					int rdr = count.get();
					if(rdr < -1)
					{
						count.compareAndSet(rdr, rdr+1);
						if(count.get() == -1)
						{
							count.compareAndSet(-1, 1); // writers waiting, release lock and notify	only when n=1													
						}
						//System.out.println("\n"+Thread.currentThread()+"Reader Unlocked - "+count.get());
						notifyAll();
						return;
					}
				} // end synchronized
			}					
		} // end while
    }
   
    public CDLListRW lockWrite() throws InterruptedException
    { 
    	while(true)
    	{
	    	if(count.get() > 1) // readers locked, flip sign as writer enqueued
			{
				synchronized(this) 
				{
					int rdr = count.get();				
					if(rdr > 1)
					{
						count.compareAndSet(rdr, (-1)*rdr);
						//System.out.println("\n"+Thread.currentThread()+"Writer Locked2 - "+count.get());	
						wait();
					}
				} 
			} 
	    	else if(count.get() == 1)
	    	{
	    		synchronized(this) 
				{
	    			if(count.get() == 1)
	    			{
	    				count.compareAndSet(1, 0);
	    				return this;
	    			}
				}
	    	}
    	} // end while   	
    }
   
    public void unlockWrite() throws InterruptedException
    {
    	while(!count.compareAndSet(0, 1));
    	synchronized(this) 
		{			
			//System.out.println("\n"+Thread.currentThread()+" ------ Writer Unlocked - "+count.get());
			notifyAll();		
		}
    }   
 }
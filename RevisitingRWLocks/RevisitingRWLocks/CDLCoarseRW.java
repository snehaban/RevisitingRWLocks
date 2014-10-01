package cse505_hw1;

/**
 * @Title   CSE505 Assignment1 - Concurrency Control
 * @Name    Sneha Banerjee
 * @UBemail snehaban@buffalo.edu
 */

public class CDLCoarseRW<T> extends CDLList<T>
{
	private Cursor reader;
	RWLock rwlock = new RWLock();
	
	public CDLCoarseRW(T v) 
	{		
		super(v);
	}	
	
	@Override
	public Cursor reader(Element from) 
	{				
		reader = new Cursor(from);		
		return reader;
	}
	
	@Override
	public void printList()
	{
		// To prevent list updates in the middle of the print operation
		rwlock.lockRead();
		super.printList();
		rwlock.unlockRead();		
	}
	
	public class Cursor extends CDLList<T>.Cursor
	{
		private Writer writer;
		
		public Cursor(Element from)
		{
			super(from);
		}	
		
		@Override
		public Element current()
		{			
			return super.current();
		}
		
		@Override
		public void previous()
		{			
			rwlock.lockRead();
			super.previous();
			rwlock.unlockRead();
		}
		
		@Override
		public void next()
		{
			rwlock.lockRead();
			super.next();
			rwlock.unlockRead();			
		}
				
		@Override		
		public Writer writer() 
		{
			writer = new Writer(current());
			return writer;
		}
	}
		
	public class Writer extends CDLList<T>.Writer
	{	
		public Writer(Element curr)
		{
			super(curr);
		}
		
		@Override
		public boolean insertBefore(T val) throws Exception
		{		
			boolean b = false;
			rwlock.lockWrite();
			b = super.insertBefore(val);
			rwlock.unlockWrite();			
			return b;
		}
		
		@Override
		public boolean insertAfter(T val) throws Exception
		{
			boolean b = false;
			rwlock.lockWrite();
			b = super.insertAfter(val);
			rwlock.unlockWrite();
			return b;
		}
		
		@Override
		public boolean delete() throws Exception
		{
			boolean b = false;
			rwlock.lockWrite();
			b = super.delete();
			rwlock.unlockWrite();
			return b;
		}
	}		
}


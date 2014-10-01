package cse505_hw1;

/**
 * @Title   CSE505 Assignment1 - Concurrency Control
 * @Name    Sneha Banerjee
 * @UBemail snehaban@buffalo.edu
 */

public class CDLListFineRW<T> extends CDLList<T>
{
	private Cursor reader;
	
	public CDLListFineRW(T v) 
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
		super.printList();
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
			if(curr == null || !curr.isValid())
			{
				//System.out.println("Invalid cursor! Element doesn't exist.");
	            return null;
			}
			else
			{
				return super.current();
			}											
		}
		
		@Override
		public void previous()
		{			
			if(current() == null || current().prev == null)
			{
				System.out.println("\nThe cursor is invalid, can't move to previous element.");
			}
			else
			{
				curr.rwLock.lockRead();
				Element e = curr;
				super.previous();
				e.rwLock.unlockRead();					
			}							
		}
		
		@Override
		public void next()
		{
			if(current() == null || current().next == null)
			{
				System.out.println("\nThe cursor is invalid, can't move to next element.");
			}
			else
			{
				curr.rwLock.lockRead();
				Element e = curr;
				super.next();
				e.rwLock.unlockRead();					
			}							
		}
		
		@Override		
		public Writer writer() 
		{
			writer = new Writer(curr);
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
			Element temp = null;
			boolean b = false;
			boolean acquired = false;
			
			if(current == null || current.prev == null)
			{
				//System.out.println("\nInvalid cursor, cannot insert ."+val);
				return false;
			}
			while(!acquired)
			{				
				temp = current.prev;
				temp.rwLock.lockWrite();											
				temp.isLocked = true;
				//System.out.print("\nAcquired outer lock on " + temp.value());
				
				if(temp.prev==null)
				{
					System.out.println("\nInvalid cursor, cannot insert "+val);
					temp.rwLock.unlockWrite();
					return false;
				}
				if(current!= null && !current.isLocked) // if unlocked
				{
					current.rwLock.lockWrite();
					current.isLocked = true;
					acquired = true;
					//System.out.print("\nAcquired inner lock on " + current.value());
					
					
					if(current.prev!=null && current.prev == temp)
					{
						b = super.insertBefore(val);
					}
					else
					{
						System.out.println("Cursor position has changed. "+val+" inserted.");
						super.insertBefore(val);
					}							
					if(temp!=null)
					{
						temp.isLocked = false;
						temp.rwLock.unlockWrite();
					}
					//System.out.print("\nReleasing inner & outer lock on " + current.value());
					if(current!=null)
					{
						current.isLocked = false;						
						current.rwLock.unlockWrite();
					}
					//return b;
				}				
				else
				{
					//System.out.print("\nReleasing outer lock on " + temp.value());
					if(temp!=null)
					{
						temp.isLocked = false; // release outer lock if unable to acquire inner lock
						temp.rwLock.unlockWrite();
					}
				}
				
				Thread.sleep(13);			
			} // end while
			return b;
		}
		
		@Override
		public boolean insertAfter(T val) throws Exception
		{
			Element temp = null;
			boolean b = false;
			boolean acquired = false;
			
			if(current == null || current.next == null)
			{
				System.out.println("\nInvalid cursor, cannot insert "+val);
				return false;
			}
			while(!acquired)
			{
				current.rwLock.lockWrite();	
				current.isLocked = true;
				//System.out.print("\nAcquired outer lock on " + current.value());				
				
				if(current.next==null)
				{
					System.out.println("\nInvalid cursor, cannot insert "+val);
					current.rwLock.unlockWrite();
					return false;
				}
				temp = current.next;
				if(!temp.isLocked) // if unlocked
				{					
					temp.rwLock.lockWrite();										
					temp.isLocked = true;
					//System.out.print("\nAcquired inner lock on " + temp.value());
					acquired = true;
					b = super.insertAfter(val);							
					
					current.isLocked = false;
					current.rwLock.unlockWrite();
					//System.out.print("\nReleasing inner & outer lock on " + temp.value());
					temp.isLocked = false;
					temp.rwLock.unlockWrite();
					//return b;
				}				
				else
				{
					current.isLocked = false;
					current.rwLock.unlockWrite();
					//System.out.print("\nReleasing outer lock on " + current.value());
				}	
				
				Thread.sleep(29);
			} // end while
			return b;
		}
		
		@Override
		public boolean delete() throws Exception
		{			
			Element prev = null, next = null;
			boolean b = false;
			boolean acquired = false;
			
			if(current == null || !current.isValid() || current.prev == null || current.next == null)
			{
				System.out.println("\nInvalid cursor, the element no longer exists, it cannot be deleted.");
				return false;
			}
			if(head() == current)  
			{					
				System.out.println("\nThe head of the circular linkedlist cannot be deleted.");
				return false;
			}
			
			while(!acquired)
			{				
				prev = current.prev;
				prev.rwLock.lockWrite();	
				if(current!=null)
				{								
					prev.isLocked = true;
					//System.out.print("\nAcquired outer lock on " + prev.value());
										
					next = current.next;
					if(next!=null && !next.isLocked) // if unlocked
					{					
						next.rwLock.lockWrite();	
													
						next.isLocked = true;							
						acquired = true;
						//System.out.print("\nAcquired inner lock on " + current.value());
						
						if(current!=null && current.prev == prev && current.next == next && next.next!=null && prev.next!=null)
						{
							b = super.delete();									
						}
						else
						{
							System.out.println("Invalid cursor. Cannot delete " + current.value());
							b = false;
						}						
						if(prev!=null)
						{
							prev.isLocked = false;
							prev.rwLock.unlockWrite();
						}
						if(next!=null)
						{
							next.isLocked = false;	
							next.rwLock.unlockWrite();	
						}
						//return b;
					}
					else
					{					
						//System.out.println("\nReleasing outer lock " + prev.value());
						prev.isLocked = false; // release outer lock if unable to acquire inner lock
						prev.rwLock.unlockWrite();
					}
				
				}// end outer if
				else
				{
					prev.rwLock.unlockWrite();
					System.out.println("Invalid cursor. Cannot delete element.");
					return false;
				}
				Thread.sleep(53);
			} // end while*/
			return b;			
		}
	}		
}

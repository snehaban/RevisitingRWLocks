package cse505_hw1;

/**
 * @Title   CSE505 Assignment1 - Concurrency Control
 * @Name    Sneha Banerjee
 * @UBemail snehaban@buffalo.edu
 */

public class CDLList<T> 
{	
	protected Element head;
	protected Cursor reader;
	protected final Element dummy;
	
	public CDLList(T v) 
	{		
		Element el = new Element(v);
		dummy = new Element(null);
		if(head == null)
		{
			head = el;
			head.next = dummy;
			head.prev = dummy;
			dummy.next = head;
			dummy.prev = head;
		}			
	}
	
	public Element head() 
	{
		return head;
	}
	
	protected Element dummy()
	{
		return dummy;
	}
	
	public Cursor reader(Element from) 
	{				
		reader = new Cursor(from);		
		return reader;
	}
	
	public void printList()
	{						
		Element temp = head();
		System.out.print("\nElements in LinkedList: ");
		System.out.print("[Head: " + head().data + "] ");
		temp = temp.next;
		while (temp != head())
		{				
			if(temp!=dummy())
				System.out.print(temp.data+ " ");
			temp = temp.next;				
		}
	}
	
	/**
	 * @class Element - stores data and next pointer of a LinkedList node
	 */
	public class Element 
	{		
		protected T data;
		protected Element next;
		protected Element prev;
		protected boolean isValid;
		protected volatile boolean isLocked;
		protected RWLock rwLock;
		
		public Element(T v)
		{
			data = v;
			next = this;
			prev = this;
			isValid = true;
			isLocked = false;
			rwLock = new RWLock();
		}
		
		public T value() 
		{
			return data; 
		}
		
		public boolean isValid() 
		{
			return isValid; 
		}
		
		public boolean isLocked() 
		{
			return isLocked; 
		}
	}
	
	/**
	 * @class Cursor - returns current element, moves to next and previous elements, returns writer
	 */
	public class Cursor 
	{
		protected Element curr;
		protected Writer writer;
		
		public Cursor(Element from)
		{
			curr = from;
		}				
		
		public Element current() 
		{			
			if(curr!=null && curr.isValid())
			{
				return curr;
			}				
			else
	        {
	            System.out.println("Invalid cursor! Element doesn't exist.");
	            return null;
	        }						
		}
				
		public void previous() 
		{			
			if(current() != null && curr.prev != null)			
			{
				if(curr.prev == dummy())
					curr = curr.prev.prev;
				else
					curr = curr.prev;
				//System.out.println("\nPrevious: Element at Cursor: "+current().data.toString());
			}
			else
			{
				System.out.println("Invalid cursor. Cannot move to previous element.");
			}
		}
		
		public void next() 
		{			
			if(current() != null && curr.next != null)
			{
				if(curr.next == dummy())
					curr = curr.next.next;
				else
					curr = curr.next;
				//System.out.println("\nNext: Element at Cursor: "+current().data.toString());
			}
			else
			{
				System.out.println("Invalid cursor. Cannot move to next element.");
			}						
		}
		
		public Writer writer() 
		{
			writer = new Writer(current());
			return writer;
		}				
	}
	
	/**
	 * @class Writer - makes updates to the LinkedList
	 */
	public class Writer 
	{	
		protected Element current;
		
		public Writer(Element curr)
		{
			current = curr;
		}
		
		public boolean insertBefore(T val) throws Exception
		{
			if(current == null || !current.isValid()  || current.prev == null)
			{
				System.out.println("The cursor is invalid, cannot insert " + val + ".");
				return false;
			}
			else
			{
				Element el = new Element(val);
				el.prev = current.prev;
				el.next = current;
				current.prev.next = el;
				current.prev = el;
				//System.out.print("  Inserted "+el.value());
				return true;
			}								
		}
		
		public boolean insertAfter(T val) throws Exception
		{
			if(current == null || !current.isValid() || current.next == null)
			{
				System.out.println("The cursor is invalid, cannot insert " + val + ".");
				return false;
			}
			else
			{
				Element el = new Element(val);
				el.next = current.next;
				el.prev = current;
				current.next.prev = el;
				current.next = el;
				//System.out.print("  Inserted "+el.value());
				return true;
			}			
		}
		
		public boolean delete() throws Exception
		{
			if(current == null || !current.isValid() || current.prev == null || current.next == null)
			{
				System.out.println("Invalid cursor, the element no longer exists, it cannot be deleted.");
				return false;
			}
		
			if(current!= head())
			{
				Element left = current.prev;
				Element right = current.next;
				left.next = right;
				right.prev = left;								
				current.isValid = false;  // to check for invalid cursors				
				System.out.println("Current element " + current.data.toString() + " deleted successfully.");
				current.next = null;
				current.prev = null;
				return true;
			}
			else
			{
				System.out.println("The head of the circular linkedlist cannot be deleted.");
				return false;
			}			
		}			
	}
}


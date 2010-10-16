package knots2.browser;

public class KnotsPage {

	private int itemCount = 0;
	private int totalPages = 0;
	private int currentPage = 0;
	
	
	public synchronized int getItemCount() {
		return itemCount;
	}
	public synchronized void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}
	public synchronized int getTotalPages() {
		return totalPages;
	}
	public synchronized void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public synchronized int getCurrentPage() {
		return currentPage;
	}
	public synchronized void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	
	
	
	
}

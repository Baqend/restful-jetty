package info.orestes.rest.conversion;

public interface Context {
	
	public <T> T getArgument(String name);
	
	public void setArgument(String name, Object value);
}

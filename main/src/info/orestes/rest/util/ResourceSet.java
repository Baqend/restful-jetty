package info.orestes.rest.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class ResourceSet extends TreeSet<String> {
	
	private static final long serialVersionUID = 1L;
	public static final Comparator<String> RESOURCE_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			if (!o1.endsWith("/")) {
				if (o2.endsWith("/")) {
					return -1;
				}
			} else if (!o2.endsWith("/")) {
				return 1;
			}
			
			return o1.compareToIgnoreCase(o2);
		}
	};
	
	public ResourceSet() {
		super(RESOURCE_COMPARATOR);
	}
	
	public ResourceSet(Collection<String> classes) {
		this();
		
		addAll(classes);
	}
}

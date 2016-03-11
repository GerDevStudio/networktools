package fr.gerdev.networktools;

/**
 * Use to provide scan results
 * You may use the type you want for the "Key"
 * The boolean object means if the key is avalaible
 * @author Ger
 */
public final class ScanResult<T> {
	private final T type;
	private final boolean isOpen;

	public ScanResult(T type, boolean isOpen) {
		super();
		this.type = type;
		this.isOpen = isOpen;
	}

	public T getEntity() {
		return type;
	}

	public boolean isOpen() {
		return isOpen;
	}
	
}

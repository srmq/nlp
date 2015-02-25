package br.ufpe.cin.srmqnlp;

public class Pair<FirstType, SecondType> {
	private FirstType first;
	private SecondType second;
	
	public Pair(FirstType first, SecondType second) {
		if (first == null || second == null) throw new IllegalStateException("Null elements are not permitted");
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof Pair<?, ?>) {
			final Pair<?, ?> pairObj = (Pair<?, ?>) obj;
			if (pairObj.first.equals(this.first) && pairObj.second.equals(this.second)) {
				result = true;
			}
		}
		return result;
	}

	public FirstType getFirst() {
		return first;
	}

	public void setFirst(FirstType first) {
		if (first == null) throw new IllegalStateException("Null elements are not permitted");
		this.first = first;
	}

	public SecondType getSecond() {
		return second;
	}

	public void setSecond(SecondType second) {
		if (second == null) throw new IllegalStateException("Null elements are not permitted");
		this.second = second;
	}
}

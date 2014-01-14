package org.mariotaku.refreshnow.widget;

public enum RefreshMode {
	NONE(0x0), START(0x1), END(0x2), BOTH(0x1 | 0x2);

	private final int flag;

	public static final int FLAG_NONE = NONE.flag;
	public static final int FLAG_START = START.flag;
	public static final int FLAG_END = END.flag;
	public static final int FLAG_BOTH = BOTH.flag;

	private RefreshMode(final int flag) {
		this.flag = flag;
	}

	public int getFlag() {
		return flag;
	}

	public boolean hasEnd() {
		return (flag & FLAG_END) != 0;
	}

	public boolean hasStart() {
		return (flag & FLAG_START) != 0;
	}

	public static RefreshMode valueOf(final int flag) {
		final boolean hasStart = (flag & FLAG_START) != 0;
		final boolean hasEnd = (flag & FLAG_END) != 0;
		if (hasStart && hasEnd) return BOTH;
		if (hasStart && !hasEnd) return START;
		if (!hasStart && hasEnd) return END;
		return NONE;
	}
}
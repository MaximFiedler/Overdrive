package io.github.bananapuncher714.overdrive.implementation.v1_15_R1;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import io.github.bananapuncher714.overdrive.api.NMSHandler;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.SystemUtils;

public class Handler_v1_15_R1 implements NMSHandler {
	private static Field nextTick;
	
	static {
		try {
			nextTick = MinecraftServer.class.getDeclaredField( "nextTick" );
			nextTick.setAccessible( true );
		} catch ( NoSuchFieldException | SecurityException e ) {
			e.printStackTrace();
		}
	}
	
	protected static long tickLength = 50L;
	
	public Handler_v1_15_R1() {
		SystemUtils.a = this::nanoTime;
	}
	
	@Override
	public long getTickDuration() {
		return tickLength;
	}

	@Override
	public void setTickDuration( long duration ) {
		tickLength = duration;		
	}
	
	private long nanoTime() {
		if ( needsUpdate() ) {
			long time = System.nanoTime() + ( TimeUnit.MILLISECONDS.toNanos( tickLength - 50L ) );
			
			try {
				long curTick = nextTick.getLong( MinecraftServer.getServer() );
				
				curTick += ( tickLength - 50L );
				
				nextTick.set( MinecraftServer.getServer(), curTick );
			} catch ( IllegalArgumentException | IllegalAccessException e ) {
				e.printStackTrace();
			}
			
			return time;
		} else {
			return System.nanoTime();
		}
	}
	
	private boolean needsUpdate() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for ( int i = 0; i < elements.length; i++ ) {
			StackTraceElement element = elements[ i ];
			int lineNumber = element.getLineNumber();
			// 826 for Spigot 1.15.1 and 1.15
			// 936 for Paper 1.15.1 and 1.15
			if ( ( lineNumber == 826 || lineNumber == 936 ) && element.getClassName().equalsIgnoreCase( MinecraftServer.class.getName() ) ) {
				return true;
			}
		}
		return false;
	}
}

package org.freecompany.redline.header;

import org.freecompany.redline.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import static org.freecompany.redline.header.Architecture.*;

public class Lead {

	public static final int LEAD_SIZE = 96;
	private static final int MAGIC = 0xEDABEEDB;

	protected byte major = 3;
	protected byte minor;
	protected RpmType type;
	protected Architecture arch = NOARCH;
	protected String name;
	protected Os os;
	protected short sigtype = 5;

	public void setMajor( byte major) {
		this.major = major;
	}

	public void setMinor( byte minor) {
		this.minor = minor;
	}

	public void setType( RpmType type) {
		this.type = type;
	}

	public void setArch( Architecture arch) {
		this.arch = arch;
	}

	public void setName( String name) {
		this.name = name;
	}

	public void setOs( Os os) {
		this.os = os;
	}

	public void setSigtype( short sigtype) {
		this.sigtype = sigtype;
	}

	public void read( ReadableByteChannel channel) throws IOException {
		ByteBuffer lead = Util.fill( channel, LEAD_SIZE);

		Util.check( MAGIC, lead.getInt());

		major = lead.get();
		minor = lead.get();
		type = RpmType.values()[ lead.getShort()];
		arch = Architecture.values()[ lead.getShort()];

		ByteBuffer data = ByteBuffer.allocate( 66);
		lead.get( data.array());
		StringBuilder builder = new StringBuilder();
		byte b;
		while (( b = data.get()) != 0) builder.append(( char) b);
		name = builder.toString();

		os = Os.values()[ lead.getShort()];
		sigtype = lead.getShort();
		if ( lead.remaining() != 16) throw new IllegalStateException( "Expected 16 remaining, found '" + lead.remaining() + "'.");
	}

	public void write( WritableByteChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate( LEAD_SIZE);
		buffer.putInt( MAGIC);
		buffer.put( major);
		buffer.put( minor);
		buffer.putShort(( short) type.ordinal());
		buffer.putShort(( short) arch.ordinal());

		byte[] data = new byte[ 66];
		System.arraycopy( name.getBytes(), 0, data, 0, name.length());
		buffer.put( data);

		buffer.putShort(( short) os.ordinal());
		buffer.putShort( sigtype);
		buffer.position( buffer.position() + 16);
		buffer.flip();
		if ( buffer.remaining() != LEAD_SIZE) throw new IllegalStateException( "Invalid lead size generated with '" + buffer.remaining() + "' bytes.");
		Util.empty( channel, buffer);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Version: ").append( major).append( ".").append( minor).append( "\n");
		builder.append( "Type: ").append( type).append( "\n");
		builder.append( "Arch: ").append( arch).append( "\n");
		builder.append( "Name: ").append( name).append( "\n");
		builder.append( "OS: ").append( os).append( "\n");
		builder.append( "Sig type: ").append( sigtype).append( "\n");
		return builder.toString();
	}
}
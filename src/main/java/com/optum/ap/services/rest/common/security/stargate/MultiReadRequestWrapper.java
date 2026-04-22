package com.optum.ap.services.rest.common.security.stargate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * @author dgoyal2
 *
 * This class will be used to read the request body multiple times
 * 
 */
public class MultiReadRequestWrapper extends HttpServletRequestWrapper {

	private ByteArrayOutputStream cachedBytes;

	public MultiReadRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (cachedBytes == null)
			cacheInputStream();

		return new CachedServletInputStream();
	}

	public void update(ByteArrayOutputStream newBytes) {
		this.cachedBytes = newBytes;
	}

	@Override
	public int getContentLength() {
		if(cachedBytes==null)
		{
			cachedBytes = new ByteArrayOutputStream();
		}
		return this.cachedBytes.size();
	}

	@Override
	public long getContentLengthLong() {
		return this.cachedBytes.size();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	private void cacheInputStream() throws IOException {
		cachedBytes = new ByteArrayOutputStream();
		IOUtils.copy(super.getInputStream(), cachedBytes);
	}

	public class CachedServletInputStream extends ServletInputStream {
		private ByteArrayInputStream input;

		public CachedServletInputStream() {
			input = new ByteArrayInputStream(cachedBytes.toByteArray());
		}

		@Override
		public boolean isFinished() {
			return input.available() != 0;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener readListener) {

			throw new UnsupportedOperationException();
		}

		@Override
		public int read() throws IOException {
			return input.read();
		}
	}
}

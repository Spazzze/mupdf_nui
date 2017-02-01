package com.artifex.mupdf.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.print.PageRange;
import android.print.pdf.PrintedPdfDocument;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.artifex.mupdf.fitz.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class PageAdapter extends BaseAdapter
{
	private final Context mContext;
	private Document mDoc;
	private volatile int loadedPageCount = 0;

	public PageAdapter(Context c)
	{
		mContext = c;
	}

	public void setDocument(Document doc)
	{
		mDoc = doc;
		final int realPageCount = mDoc.countPages();
		loadedPageCount = 0;

		//  load the first page
		mDoc.loadPage(loadedPageCount);
		loadedPageCount++;

		if (realPageCount>1)
		{
			// load the rest
			final Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable()
			{
				@Override
				public void run()
				{
					//  load a page
					mDoc.loadPage(loadedPageCount);
					loadedPageCount++;

					//  delay loading of the next page a little, to keep the UI responsive.
					if (loadedPageCount<realPageCount)
						handler.postDelayed(this, 2);
				}
			});
		}
	}

	private int mWidth;
	public void setWidth(int w)
	{
		mWidth = w;
	}

	@Override
	public int getCount()
	{
		//  return the number of pages that have been loaded so far.
		return loadedPageCount;
	}

	public Object getItem(int position)
	{
		return null; // not used
	}

	public long getItemId(int position)
	{
		return 0; // not used
	}

	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// make or reuse a view
		DocPageView pageView;

		final Activity activity = (Activity) mContext;
		if (convertView == null)
		{
			// make a new one
			pageView = new DocPageView(activity, mDoc);
		}
		else
		{
			// reuse an existing one
			pageView = (DocPageView) convertView;
		}

		// set up the page
		pageView.setupPage(position, mWidth, 1);

		return pageView;
	}
}

package com.app.demos.sqlite;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.app.demos.base.BaseSqlite;
import com.app.demos.model.Blog;

public class BlogSqlite extends BaseSqlite {

	private Context c;
	
	public BlogSqlite(Context context) {
		super(context);
		c = context;
	}

	@Override
	protected String tableName() {
		return "blogs";
	}

	@Override
	protected String[] tableColumns() {
		String[] columns = {
			Blog.COL_ID,
			Blog.COL_FACE,
			Blog.COL_CONTENT,
			Blog.COL_COMMENT,
			Blog.COL_AUTHOR,
			Blog.COL_UPTIME,
			Blog.COL_LATITUDE,
			Blog.COL_LONGITUDE
		};
		return columns;
	}

	@Override
	protected String createSql() {
		return "CREATE TABLE " + tableName() + " (" +
			Blog.COL_ID + " INTEGER PRIMARY KEY, " +
			Blog.COL_FACE + " TEXT, " +
			Blog.COL_CONTENT + " TEXT, " +
			Blog.COL_COMMENT + " TEXT, " +
			Blog.COL_AUTHOR + " TEXT, " +
			Blog.COL_UPTIME + " TEXT, " +
			Blog.COL_LATITUDE + " TEXT, " +
			Blog.COL_LONGITUDE + " TEXT" +
			");";
	}

	@Override
	protected String upgradeSql() {
		return "DROP TABLE IF EXISTS " + tableName();
	}

	/*public void upgradeTable() {
		DbHelper dbh1 = new DbHelper(c, "demos.db", null, 1);
		SQLiteDatabase db1 = dbh1.getWritableDatabase();
		dbh1.onUpgrade(db1, 1, 1);
		
	}*/
	
	public boolean updateBlog (Blog blog) {
		// prepare blog data
		ContentValues values = new ContentValues();
		values.put(Blog.COL_ID, blog.getId());
		values.put(Blog.COL_FACE, blog.getFace());
		values.put(Blog.COL_CONTENT, blog.getContent());
		values.put(Blog.COL_COMMENT, blog.getComment());
		values.put(Blog.COL_AUTHOR, blog.getAuthor());
		values.put(Blog.COL_UPTIME, blog.getUptime());
		values.put(Blog.COL_LATITUDE, blog.getLatitude());
		values.put(Blog.COL_LONGITUDE, blog.getLongitude());
		// prepare sql
		String whereSql = Blog.COL_ID + "=?";
		String[] whereParams = new String[]{blog.getId()};
		// create or update
		try {
			if (this.exists(whereSql, whereParams)) {
				this.update(values, whereSql, whereParams);
			} else {
				this.create(values);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public ArrayList<Blog> getAllBlogs () {
		ArrayList<Blog> blogList = new ArrayList<Blog>();
		try {
			ArrayList<ArrayList<String>> rList = this.query(null, null);
			int rCount = rList.size();
			for (int i = 0; i < rCount; i++) {
				ArrayList<String> rRow = rList.get(i);
				Blog blog = new Blog();
				blog.setId(rRow.get(0));
				blog.setFace(rRow.get(1));
				blog.setContent(rRow.get(2));
				blog.setComment(rRow.get(3));
				blog.setAuthor(rRow.get(4));
				blog.setUptime(rRow.get(5));
				blog.setLatitude(rRow.get(6));
				blog.setLongitude(rRow.get(7));
				blogList.add(blog);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return blogList;
	}
}
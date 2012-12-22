package com.lsg.app.tasks;

public interface TaskSelected {
	public static int TASK_EXAMS = 0;
	public static int TASK_HOMEWORK = 1;
	public static int TASK_GRADES = 2;
	public static int TASK_EDIT_EXAMS = 3;
	public static final String ID = "id";
	public int getCurId();
	public int getCurTask();
	public void onTaskSelected(int taskId);
	public void onTaskSelected(int taskId, int rowId);
}

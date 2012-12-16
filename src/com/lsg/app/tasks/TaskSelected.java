package com.lsg.app.tasks;

public interface TaskSelected {
	public static int TASK_EXAMS = 0;
	public static int TASK_HOMEWORK = 1;
	public static int TASK_GRADES = 2;
	public void onTaskSelected(int taskId);
}

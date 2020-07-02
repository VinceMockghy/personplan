package cn.edu.zucc.personplan.model;

public class BeanPlan {
	public static final String[] tableTitles={"���","����","������","�������"};
	/**
	 * �����и���javabean������޸ı��������룬col��ʾ�������е�����ţ�0��ʼ
	 */
	public int plan_id;
	public String plan_name;
	public int step_count;
	public int finished_step_count;

	public BeanPlan(int plan_id, String plan_name, int step_count, int finished_step_count) {
		this.plan_id = plan_id;
		this.plan_name = plan_name;
		this.step_count = step_count;
		this.finished_step_count = finished_step_count;
	}

	public int getPlan_id() {
		return plan_id;
	}

	public void setPlan_id(int plan_id) {
		this.plan_id = plan_id;
	}

	public String getPlan_name() {
		return plan_name;
	}

	public void setPlan_name(String plan_name) {
		this.plan_name = plan_name;
	}

	public int getStep_count() {
		return step_count;
	}

	public void setStep_count(int step_count) {
		this.step_count = step_count;
	}

	public int getFinished_step_count() {
		return finished_step_count;
	}

	public void setFinished_step_count(int finished_step_count) {
		this.finished_step_count = finished_step_count;
	}

	public String getCell(int col){
		if(col==0) return "1";
		else if(col==1) return "ʾ���ƻ�";
		else if(col==2) return "2";
		else if(col==3) return "1";
		else return "";
	}

}

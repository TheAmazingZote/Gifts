package me.zote.gifts.items.menus;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class Paginator<T> {

	private List<T> objects;
	private double pagSize;
	private int currentPage = 1;
	private int amountOfPages;

	public Paginator(T[] objects, Integer max) {
		this.objects = Lists.newArrayList(objects);
		pagSize = max;
		amountOfPages = (int) Math.ceil(objects.length / pagSize);
	}

	public Paginator(int max) {
		this((T[]) new Object[0], max);
	}

	public void setElements(List<T> objectList) {
		objects = objectList;
		amountOfPages = (int) Math.ceil(objects.size() / pagSize);
	}

	public void addElement(T element) {
		objects.add(element);
		amountOfPages = (int) Math.ceil(objects.size() / pagSize);
	}
	
	public void delElement(T element) {
		objects.remove(element);
		amountOfPages = (int) Math.ceil(objects.size() / pagSize);
	}

	public boolean hasNext() {
		return currentPage < amountOfPages;
	}

	public boolean hasPrev() {
		return currentPage > 1;
	}

	public int getNext() {
		return currentPage + 1;
	}

	public int getPrev() {
		return currentPage - 1;
	}

	public int getTotalPages() {
		return amountOfPages;
	}

	public int getCurrent() {
		return currentPage;
	}
	
	public void setCurrent(int pageNum) {
		currentPage = pageNum;
	}

	public List<T> getPage() {
		List<T> page = new ArrayList<>();

		if (objects.size() == 0)
			return page;

		double startC = pagSize * (currentPage - 1);
		double finalC = startC + pagSize;

		for (; startC < finalC; startC++)
			if (startC < objects.size())
				page.add(objects.get((int) startC));
		return page;
	}

}

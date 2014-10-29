package com.hollysys.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;

public class ParseXML {
	private static Element Root;
	private static String fileName = "NaviMenuInfo.xml";
	private static Element currentElement;

	// 解析文件，获取根节点
	public static void parseXml(Context context) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		// 从dom工厂实例获得dom解析器
		DocumentBuilder builder = null;
		Document xmldoc = null;
		try {
			builder = factory.newDocumentBuilder();
			xmldoc = builder.parse(context.getAssets().open(fileName));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		Root = (Element)xmldoc.getDocumentElement().getElementsByTagName("Domain").item(0);
	}

	// 获取根节点
	public static Element getRoot() {
		return Root;
	}

	// 查找节点，并返回第一个符合条件节点
	public static Node selectSingleNode(String express, Object source) {
		Node result = null;
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		try {
			result = (Node) xpath
					.evaluate(express, source, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			System.out.print(e.getMessage());
		}
		return result;
	}
	//查找子节点
	public static List<Element> findChild(Element node){
		NodeList child = node.getChildNodes();
		List<Element> list = new ArrayList<Element>();
		for (int i = 0; i < child.getLength(); i++)
		 {
		    Node childNode = child.item(i);
		    if (childNode instanceof Element)
		    {
		       Element childElement = (Element)childNode;
		       list.add(childElement);
		    }
		 }
		return list;
	}

	/**
	 * 设置所有的属性值为一个值
	 * @param attrName 属性名称
	 * @param value 值
	 */
	public static void setAllElementAttr(String attrName, String value){
		if(null == Root)
			return;
		NodeList list = Root.getElementsByTagName("MenuItem");
		for(int i=0; i<list.getLength(); i++){
			Element e = (Element)list.item(i);
			e.setAttribute(attrName, value);
		}
	}
	
	/**
	 * 将与list给出的Page属性值相等的元素的IsAlarmming属性设为1
	 * @param list
	 */
	public static void setIsAlarmmingEqualsOne(List<String> list){
		if(list.isEmpty() || null==Root)
			return;
		for(String page : list){
			loopSetElement(Root, page);
		}
	}

	/**
	 * 递归设置IsAlarmming值
	 * @param element
	 * @param page
	 * @return
	 */
	private static boolean loopSetElement(Element element, String page) {
		boolean isSetOne = false;
		if(element.getAttribute("Page").equals(page)){
			element.setAttribute("IsAlarmming", "1");
			return true;
		}
		List<Element> childList = findChild(element);
		if(childList.isEmpty())
			return false;
		for(Element e : childList){
			if(loopSetElement(e,page))
				isSetOne=true;	
		}
		if(isSetOne)
			element.setAttribute("IsAlarmming", "1");
		return isSetOne;
	}
	
	/**
	 * 得到当前使用的Element
	 * @return
	 */
	public static Element getCurrentElement() {
		return currentElement;
	}

	/**
	 * 设置当前使用的Element
	 * @param currentElement
	 */
	public static void setCurrentElement(Element currentElement) {
		ParseXML.currentElement = currentElement;
	}
}

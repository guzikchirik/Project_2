package app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Phone {
	
	// Данные записи о телефоне.
	private String id = "";
	private String owner = "";
	private String number = "";	

	public Phone(String id, String owner, String number)
	{
		this.id = id;
		this.owner = owner;
		this.number = number;
	}
	
	// Конструктор для создания пустой записи телефона.
	public Phone()
	{
		this.id = "0";
		this.owner = "";
		this.number = "";		
	}	

	// Конструктор для создания записи, предназначенной для добавления в БД. 
	public Phone(String owner, String number)
	{
		this.id = "0";
		this.owner = owner;
		this.number = number;		
	}

	// Валидация номера. 
	public boolean validatePersonNumberPart(String number_part)
	{	
	    	Matcher matcher = Pattern.compile("[0-9-+\\#]{2,50}").matcher(number_part);
	    	return matcher.matches(); 	    
	}
	
	// ++++++++++++++++++++++++++++++++++++++
	// Геттеры и сеттеры
	public String getId()
	{
		return this.id;
	}
	
	public String getOwner()
	{
		return this.owner;
	}

	public String getNumber()
	{
		return this.number;
	}	

	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public void setNumber(String number)
	{
		this.number = number;
	}	
	
}


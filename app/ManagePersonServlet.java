package app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ManagePersonServlet extends HttpServlet {
	
	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;
	
	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;
       
    public ManagePersonServlet()
    {
        // Вызов родительского конструктора.
    	super();
		
    	// Создание экземпляра телефонной книги.
        try
		{
			this.phonebook = Phonebook.getInstance();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}        
        
    }
    
  //переход на указанную JSP-страницу 
    protected void jump(String url, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    { 
    	RequestDispatcher rd = getServletContext().getRequestDispatcher(url); 
      	rd.forward(request, response); 
     } 

    // Валидация ФИО и генерация сообщения об ошибке в случае невалидных данных.
    private String validatePersonFMLName(Person person)
    {
		String error_message = "";
		
		if (!person.validateFMLNamePart(person.getName(), false))
		{
			error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		if (!person.validateFMLNamePart(person.getSurname(), false))
		{
			error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		if (!person.validateFMLNamePart(person.getMiddlename(), true))
		{
			error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
		}
		
		return error_message;
    }
    
 // Валидация телефонного номера и генерация сообщения об ошибке в случае невалидных данных.
    private String validatePersonNumber(Phone phone)
    {
		String error_message = "";		
		if (!phone.validatePersonNumberPart(phone.getNumber()))
		{
			error_message += "Телефонный номер должен быть от 2 до 50 символов: цифра, +, -, #.<br />";
		}		
		return error_message;
    }
    
    // Реакция на GET-запросы.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");
		
		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);
		
		// Хранилище параметров для передачи в JSP.
		HashMap<String,String> jsp_parameters = new HashMap<String,String>();

		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
        RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
        RequestDispatcher dispatcher_for_manager_phone = request.getRequestDispatcher("/ManagePhone.jsp");

		// Действие (action) и идентификатор записи (id) над которой выполняется это действие.
		String action = request.getParameter("action");		
		String id = request.getParameter("id");
				
		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
        if ((action == null)&&(id == null))
        {
        	request.setAttribute("jsp_parameters", jsp_parameters);
            dispatcher_for_list.forward(request, response);
        }
        // Если же действие указано, то...
        else
        {
        	switch (action)
        	{        			
        		// Добавление записи.
        		case "add":
        			// Создание новой пустой записи о пользователе.
        			Person empty_person = new Person();  //person id =0 а все остальные поля пустые
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "add");
        			jsp_parameters.put("next_action", "add_go");
        			jsp_parameters.put("next_action_label", "Добавить");
        			
        			// Установка параметров JSP.
        			request.setAttribute("person", empty_person);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.        			
        			jump("/ManagePerson.jsp", request, response);
        		break;
			
        		// Редактирование записи.
        		case "edit":
        			// Извлечение из телефонной книги информации о редактируемой записи.        			
        			Person editable_person = this.phonebook.getPerson(id);
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "edit");
        			jsp_parameters.put("next_action", "edit_go");
        			jsp_parameters.put("person_id", editable_person.getId());
        			jsp_parameters.put("next_action_label", "Сохранить");

        			// Установка параметров JSP.
        			request.setAttribute("person", editable_person);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
        		break;
			
        		// Удаление записи.
        		case "delete":
        			
        			// Если запись удалось удалить...
        			if (phonebook.deletePerson(id))
        			{
        				jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
        				jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
        			}
        			// Если запись не удалось удалить (например, такой записи нет)...
        			else
        			{
        				jsp_parameters.put("current_action_result", "DELETION_FAILURE");
        				jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
        			}

        			// Установка параметров JSP.
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_list.forward(request, response);
       			break;
       		
       			// Добавление ТЕЛЕФОНА
        		case "add_phone":
        			// Создание новой пустой записи телефона.
        			Person editable_person1 = this.phonebook.getPerson(id);
        			Phone empty_phone = new Phone();
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "add_phone");
        			jsp_parameters.put("next_action", "add_phone_go");
        			jsp_parameters.put("person_id", editable_person1.getId());        			
        			jsp_parameters.put("next_action_label", "Добавить");        			
        			
        			// Установка параметров JSP.
        			request.setAttribute("phone", empty_phone);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager_phone.forward(request, response);
        		break;
        		
        		// Редактирование ТЕЛЕФОНА.
        		case "edit_phone":
        			// Извлечение из телефонной книги информации о редактируемой записи.         			
        			Phone editable_phone = this.phonebook.getPhone(id);  //достаю редактируемый телефон
        			
        			// Подготовка параметров для JSP.
        			jsp_parameters.put("current_action", "edit_phone");
        			jsp_parameters.put("next_action", "edit_phone_go");    
        			jsp_parameters.put("person_id", editable_phone.getOwner()); // достаю ID редактируемого человека но эт вроде не нужно
        			jsp_parameters.put("next_action_label", "Сохранить");

        			// Установка параметров JSP.
        			request.setAttribute("phone", editable_phone);
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager_phone.forward(request, response);
        		break;
        		
        		// Удаление ТЕЛЕФОНА.
        		case "delete_phone":
        			Phone deleted_phone = this.phonebook.getPhone(id);
        			// Если запись удалось удалить...
        			if (phonebook.deletePhone(id))
        			{
        				jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
        				jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
        				jsp_parameters.put("current_action", "edit");
	        			jsp_parameters.put("next_action", "edit_go");	        						
	        			jsp_parameters.put("next_action_label", "Сохранить");        			
	        			
	        			// Установка параметров JSP.		
	        			request.setAttribute("person", phonebook.getPerson(deleted_phone.getOwner()));
        			}
        			// Если запись не удалось удалить (например, такой записи нет)...
        			else
        			{
        				jsp_parameters.put("current_action_result", "DELETION_FAILURE");
        				jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
        			}

        			// Установка параметров JSP.
        			request.setAttribute("jsp_parameters", jsp_parameters);
        			
        			// Передача запроса в JSP.
        			dispatcher_for_manager.forward(request, response);
       			break;
       		}
        }
		
	}

	// Реакция на POST-запросы.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// Обязательно ДО обращения к любому параметру нужно переключиться в UTF-8,
		// иначе русский язык при передаче GET/POST-параметрами превращается в "кракозябры".
		request.setCharacterEncoding("UTF-8");

		// В JSP нам понадобится сама телефонная книга. Можно создать её экземпляр там,
		// но с архитектурной точки зрения логичнее создать его в сервлете и передать в JSP.
		request.setAttribute("phonebook", this.phonebook);
		
		// Хранилище параметров для передачи в JSP.
		HashMap<String,String> jsp_parameters = new HashMap<String,String>();	
		// Диспетчеры для передачи управления на разные JSP (разные представления (view)).
		RequestDispatcher dispatcher_for_manager = request.getRequestDispatcher("/ManagePerson.jsp");
		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_manager_phone = request.getRequestDispatcher("/ManagePhone.jsp");	
		
		
		// Действие (add_go, edit_go) и идентификатор записи (id) над которой выполняется это действие.
		String add_go = request.getParameter("add_go");		
		String edit_go = request.getParameter("edit_go");		
		String add_phone_go = request.getParameter("add_phone_go");		
		String edit_phone_go = request.getParameter("edit_phone_go");			
		String owner = request.getParameter("owner");			
		
		// Добавление записи.
		if (add_go != null)
		{
			// Создание записи на основе данных из формы.
			//берем значения из полей	
			Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"), request.getParameter("middlename"));		
			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(new_person); 
			
			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{

				// Если запись удалось добавить...
				if (this.phonebook.addPerson(new_person))
				{					
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				}
				// Если запись НЕ удалось добавить...
				else
				{
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
	        
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{
    			// Подготовка параметров для JSP.
    			jsp_parameters.put("current_action", "add");
    			jsp_parameters.put("next_action", "add_go");
    			jsp_parameters.put("next_action_label", "Добавить");
    			jsp_parameters.put("error_message", error_message);
    			
    			// Установка параметров JSP.
    			request.setAttribute("person", new_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			// Передача запроса в JSP.
    			dispatcher_for_manager.forward(request, response);
			}
		}
		
		// Добавление телефона.
				if (add_phone_go != null)
				{
					// Создание записи на основе данных из формы.
					Phone new_phone = new Phone(request.getParameter("owner"), request.getParameter("number"));	//это owner и number	
					Person editable_person = this.phonebook.getPerson(owner);
					// Валидация ТЕЛЕФОНА.
					String error_message = this.validatePersonNumber(new_phone); 
					
					// Если данные верные, можно производить добавление.
					if (error_message.equals(""))
					{

						// Если запись удалось добавить...
						if (this.phonebook.addPhone(new_phone))
						{							
							jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
							jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
							jsp_parameters.put("Добавил телефон", "А ГДЕ, БЛИАТЬ, ОН???");
							jsp_parameters.put("current_action", "edit");
		        			jsp_parameters.put("next_action", "edit_go");
		        			jsp_parameters.put("person_id", request.getParameter("id"));       //не нужно пока... 			
		        			jsp_parameters.put("next_action_label", "Сохранить");        			
		        			
		        			// Установка параметров JSP.		
		        			request.setAttribute("person", phonebook.getPerson(request.getParameter("owner")));
		        			request.setAttribute("jsp_parameters", jsp_parameters);
						}
						// Если запись НЕ удалось добавить...
						else
						{
							jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
							jsp_parameters.put("current_action_result_label", "Ошибка добавления");
						}

						// Установка параметров JSP.
						
						request.setAttribute("jsp_parameters", jsp_parameters);
			        
						// Передача запроса в JSP.
						dispatcher_for_manager.forward(request, response);
					}
					// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
					else
					{
		    			// Подготовка параметров для JSP.
		    			jsp_parameters.put("current_action", "add_phone");
		    			jsp_parameters.put("next_action", "add_phone_go");
		    			jsp_parameters.put("person_id", editable_person.getId());
		    			jsp_parameters.put("next_action_label", "Добавить");
		    			jsp_parameters.put("error_message", error_message);
		    			
		    			// Установка параметров JSP.
		    			request.setAttribute("phone", new_phone);
		    			request.setAttribute("jsp_parameters", jsp_parameters);
		    			
		    			// Передача запроса в JSP.
		    			dispatcher_for_manager_phone.forward(request, response);
					}
				}
		
		// Редактирование записи.
		if (edit_go != null)
		{
			// Получение записи и её обновление на основе данных из формы.
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id")); 
			String surname_of_updatable_person = updatable_person.getSurname();
			String name_of_updatable_person = updatable_person.getName();
			String middlename_of_updatable_person = updatable_person.getMiddlename();	
			
			updatable_person.setName(request.getParameter("name"));
			updatable_person.setSurname(request.getParameter("surname"));
			updatable_person.setMiddlename(request.getParameter("middlename"));

			// Валидация ФИО.
			String error_message = this.validatePersonFMLName(updatable_person); 
			
			// Если данные верные, можно производить добавление.
			if (error_message.equals(""))
			{
			
				// Если запись удалось обновить...
				if (this.phonebook.updatePerson( updatable_person))
				{
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				}
				// Если запись НЕ удалось обновить...
				else
				{
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}

				// Установка параметров JSP.
				request.setAttribute("jsp_parameters", jsp_parameters);
	        
				// Передача запроса в JSP.
				dispatcher_for_list.forward(request, response);
			}
			// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
			else
			{
				updatable_person.setName(name_of_updatable_person);
				updatable_person.setSurname(surname_of_updatable_person);
				updatable_person.setMiddlename(middlename_of_updatable_person);
				
    			// Подготовка параметров для JSP.
    			jsp_parameters.put("current_action", "edit");
    			jsp_parameters.put("next_action", "edit_go");
    			jsp_parameters.put("next_action_label", "Сохранить");
    			jsp_parameters.put("error_message", error_message);

    			// Установка параметров JSP.
    			request.setAttribute("person", updatable_person);
    			request.setAttribute("jsp_parameters", jsp_parameters);
    			
    			// Передача запроса в JSP.
    			dispatcher_for_manager.forward(request, response);    			
    			
			}
		}
		// Редактирование записи телефона.
				if (edit_phone_go != null)
				{
					// Получение записи и её обновление на основе данных из формы.
					
					Phone updatable_phone = this.phonebook.getPhone(request.getParameter("id")); 					 
					updatable_phone.setOwner(request.getParameter("owner"));					
					String current_number = updatable_phone.getNumber(); //запоминаем...
					updatable_phone.setNumber(request.getParameter("number"));			//берём из поля ввода		
					
					// Валидация Телефона.
					String error_message = this.validatePersonNumber(updatable_phone); 					
					// Если данные верные, можно производить добавление.
					if (error_message.equals(""))
					{
					
						// Если запись удалось обновить...
						if (this.phonebook.updatePhone(updatable_phone))
						{
							jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
							jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
							jsp_parameters.put("current_action", "edit");
		        			jsp_parameters.put("next_action", "edit_go");
		        			jsp_parameters.put("person_id", owner);       //не нужно пока... 			
		        			jsp_parameters.put("next_action_label", "Сохранить");        			
		        			
		        			// Установка параметров JSP.		
		        			request.setAttribute("person", phonebook.getPerson(owner));
						}
						// Если запись НЕ удалось обновить...
						else
						{
							jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
							jsp_parameters.put("current_action_result_label", "Ошибка обновления");
						}

						// Установка параметров JSP.
						request.setAttribute("jsp_parameters", jsp_parameters);
			        
						// Передача запроса в JSP.
						dispatcher_for_manager.forward(request, response);
					}
					// Если в данных были ошибки, надо заново показать форму и сообщить об ошибках.
					else
					{						
		    			// Подготовка параметров для JSP.
						jsp_parameters.put("current_action", "edit_phone");
	        			jsp_parameters.put("next_action", "edit_phone_go");
	        			jsp_parameters.put("person_id", request.getParameter("owner"));       //не нужно пока... 			
	        			jsp_parameters.put("next_action_label", "Сохранить");   
	        			jsp_parameters.put("error_message", error_message);
	        			updatable_phone.setNumber(current_number);
	        			// Установка параметров JSP.		        					    			
		    			request.setAttribute("phone", updatable_phone);
		    			request.setAttribute("jsp_parameters", jsp_parameters);
		    			
		    			// Передача запроса в JSP.
		    			dispatcher_for_manager_phone.forward(request, response);    			
		    			
					}
				}
	}
}

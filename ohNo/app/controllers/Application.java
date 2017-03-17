package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Routes;
import play.libs.Json;
import play.mvc.*;
import play.data.*;
import models.*;

import java.util.List;

import static play.libs.Json.toJson;

public class Application extends Controller {
    //Часть 1. Методы, реализованные за вас.

    static Form<Note> noteForm = Form.form(Note.class);

    /**
     * Точка входа в приложение
     *
     * @return Redirect to Application.notes()
     */
    public static Result index() {
        //используем реверсную маршрутизацию для генерации строки URL из action-а контролера
        return redirect(controllers.routes.Application.notes());
    }


    /**
     * Отдаем базовый шаблон одностраничного приложения
     */
    public static Result notes() {
        return ok(
                views.html.index.render(noteForm)
        );
    }


    /**
     * Возвращает результат в виде Json с ошибкой, упакованный с соответствующими заголовками.
     *
     * @param errorMessage текст ошибки, передаваемый на клиент
     * @return
     */
    private static Result errorJsonResult(String errorMessage) {
        return badRequest(errorJson(errorMessage));
    }

    /**
     * Возвращает Json с сообщение об ошибке.
     */
    private static JsonNode errorJson(String errorMessage) {
        return Json.newObject().put("error", errorMessage);
    }

    //API для взаимодействия клиентов с сервером посредством JSON

    /**
     * Контроллер возвращает список записей в формате Json
     *
     * @return список записей в формате Json
     */
    public static Result notesJson() {
        List<Note> all = Note.all();
        return ok(toJson(all));  //toJson преобразует объект или список объектов в соответствующий JSON
    }


    //Часть2. Методы, которые нужно реализовать.

    /**
     * Удаляет запись с сервера. Запрос приходит в формате  Json.
     * <p/>
     * Необходимо его распарсить и проверить на корректность, а также обработать ошибки в случае отсутствия в базе данных
     *
     * @return Возвращает удаленную запись в случае, если все ок; возвращает badRequest с ошибкой в случае
     * некорректных параметров; возвращает notFound с ошибкой, если параметры корректны, но в базе данных такой записи нет.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result deleteNoteJson() {
        //журнализация. С помощью метода info можем в журнал внести любую запись.
        play.Logger.info("deleteNoteJson()");
        //парсим запрос в Json
        JsonNode json = request().body().asJson();

        if (json == null) {
            //некорректный запрос, возвращаем ответ с ошибкой. В качестве параметра передаем текст ошибки.
            return errorJsonResult("Json expected");
        } else {
            Long id = null;
            try {
                //получение значение атрибута с именем id и преобразование в Long. Может отсуствовать или быть в другом формате!!!
                id = json.findPath("id").longValue();
            } catch (NumberFormatException nfe) {
                //Не находится в формате числа
                //todo выдать сообщение с ошибкой "id must be an integer"
                return errorJsonResult("id must be an integer");
            } finally {
                if (id == null) {
                    //todo вдыать сообщение с ошибкой "id must be specified"
                    return errorJsonResult("id must be specified");
                }
            }
            assert(id!=null);

            Note note = Note.find(id);
            play.Logger.debug(note.toString());//todo получить из базы запись по id
            if (note == null) {
                //Ответ с кодом 404 и телом в виде Json
                return notFound(errorJson("note is not found"));
            }
            //Сохраняем запись note в json для ответа на клиент.
            JsonNode result = Json.toJson(note); //todo

            // удаляем запись
            //todo
            Note.delete(id);

            //возвращаем в формате JSON результат (используем переменную result)
            return ok(result);
        }
    }

    /**
     * Метод прозводит создание записей в базу данных либо обновление существующей записи.
     *
     * На вход принимается JSON. Если JSON содержит id не нулевой и в нужном формате, то ищем в базе и редактируем.
     * Если же id отсутствует (равно null) или равен 0, то создаем новую запись.
     * Если id число - ищем в базе данных.
     * В случае, если не нашли, выдаем ошибку.
     * В случае если все ок - производим операцию редактирования
     *
     * @return Возвращает созданную или отредактированную запись. В случае некорректных данных возвращает ошибку.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result saveNoteJson() {
        play.Logger.info("saveNoteJson()");

        JsonNode json = request().body().asJson();

        if (json == null) {
            return errorJsonResult("JSON expected"); //todo выдать сообщение с ошибкой "JSON expected";
        } else {
            //todo Получить Note из json-запроса. Особо отнестись к id

            Long id = null; //todo
            try {
                id = json.findPath("id").longValue();
            } catch (NumberFormatException nfe) {
                return errorJsonResult("id must be an integer");
            } finally {
                if (id == null) {
                    return errorJsonResult("id must be specified");
                }
            }
            Note note = null;
            if (id != 0) {
                note = Note.find(id);
            }
            if (note == null) {
                //todo создаем новый Note
                note = new Note();
            }


            note.cellPhone = json.findPath("cellPhone").asText();
            note.homePhone = json.findPath("homePhone").asText();
            note.name = json.findPath("name").asText();//ищем в json значение параметра cellPhone и возвращаем в качестве строки
            //todo homePhone и name

            //сохраняем новый объект или редактируем старый
            play.Logger.info("trying to save to DB");
            note.save();

            //todo возвращаем в формате JSON созданную или отредактированную запись.
            return ok(Json.toJson(note));
        }
    }


    /**
     * Контроллер выдает JavaScript файл для поддержки реверсной маршрутизации на клиенте.
     * @return JS скрипт, генерирующий создание объекта jsRoutes для переданного списка контроллеров.
     */
    public static Result jsRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.routes.javascript.Application.notes(),
                        controllers.routes.javascript.Application.notesJson(),
                        controllers.routes.javascript.Application.saveNoteJson(),
                        controllers.routes.javascript.Application.deleteNoteJson()
                        //todo добавить в роутер необходимые контроллеры для API - создание записи, редактирование, удаление...
                )
        );
    }
}

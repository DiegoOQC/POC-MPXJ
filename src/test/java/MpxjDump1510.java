

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.mpxj.Duration;
import org.mpxj.ProjectCalendar;
import org.mpxj.ProjectFile;
import org.mpxj.ProjectProperties;
import org.mpxj.Resource;
import org.mpxj.ResourceAssignment;
import org.mpxj.Task;
import org.mpxj.primavera.PrimaveraPMFileReader;

public class MpxjDump1510 {

    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\diego.costa\\Downloads\\Cronograma Corrente.xml"; // <-- ajuste
        Integer projectId = null;              // <-- se houver múltiplos projetos no XML, informe o ID aqui

        ProjectFile file = readPrimaveraP6Xml(path, projectId);

        dumpProjectFile(file, "PROJETO");
        dumpBaselinesCompat(file);
    }

    // =========================
    // READ
    // =========================
    public static ProjectFile readPrimaveraP6Xml(String path, Integer projectId) throws Exception {
        PrimaveraPMFileReader reader = new PrimaveraPMFileReader();
        if (projectId != null) {
            reader.setProjectID(projectId);
        }
        return reader.read(path);
    }
    
    public static void dumpAllProjectsFromP6Xml(String path) throws Exception {
    List<Integer> projectIds = listProjectIdsFromP6XmlCompat(path);

    if (projectIds.isEmpty()) {
        System.out.println("Não consegui descobrir os ProjectIDs automaticamente.");
        System.out.println("Você pode informar manualmente, ex:");
        System.out.println("dumpProjectsByIds(path, Arrays.asList(123, 456));");
        return;
    }

    System.out.println("\n============================================================");
    System.out.println("PROJETOS ENCONTRADOS NO XML: " + projectIds);
    System.out.println("============================================================");

    for (Integer id : projectIds) {
        PrimaveraPMFileReader reader = new PrimaveraPMFileReader();
        reader.setProjectID(id);

        ProjectFile file = reader.read(path);

        // usa seu dump geral existente
        dumpProjectFile(file, "PROJECT ID=" + id);
    }
}

/**
 * Tenta listar os ProjectIDs existentes no P6 XML usando possíveis métodos do reader.
 * Isso evita depender de um nome exato de API (que muda entre versões).
 */
public static List<Integer> listProjectIdsFromP6XmlCompat(String path) throws Exception {
    PrimaveraPMFileReader reader = new PrimaveraPMFileReader();

    // Tentativas comuns (variantes de API)
    // - getProjectIDs(String path)
    // - getProjectIdList(String path)
    // - listProjectIDs(String path)
    // - getProjectIDs(File file) ... etc
    Object result;

    result = tryInvokeReader(reader, "getProjectIDs", new Class<?>[]{String.class}, new Object[]{path});
    if (result == null) result = tryInvokeReader(reader, "getProjectIdList", new Class<?>[]{String.class}, new Object[]{path});
    if (result == null) result = tryInvokeReader(reader, "listProjectIDs", new Class<?>[]{String.class}, new Object[]{path});
    if (result == null) result = tryInvokeReader(reader, "listProjectIdList", new Class<?>[]{String.class}, new Object[]{path});

    // Normaliza para List<Integer>
    return normalizeToIntegerList(result);
}

private static Object tryInvokeReader(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
    try {
        Method m = target.getClass().getMethod(methodName, paramTypes);
        return m.invoke(target, args);
    } catch (Exception e) {
        return null;
    }
}

@SuppressWarnings("unchecked")
private static List<Integer> normalizeToIntegerList(Object obj) {
    if (obj == null) return Collections.emptyList();

    // já é List
    if (obj instanceof List) {
        List<?> list = (List<?>) obj;
        List<Integer> out = new ArrayList<>();
        for (Object v : list) {
            Integer id = toInt(v);
            if (id != null) out.add(id);
        }
        return out;
    }

    // array
    if (obj.getClass().isArray()) {
        int len = java.lang.reflect.Array.getLength(obj);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            Object v = java.lang.reflect.Array.get(obj, i);
            Integer id = toInt(v);
            if (id != null) out.add(id);
        }
        return out;
    }

    // Map (às vezes vem Map<ID, nome> ou algo assim)
    if (obj instanceof Map) {
        Map<?, ?> map = (Map<?, ?>) obj;
        List<Integer> out = new ArrayList<>();
        for (Object k : map.keySet()) {
            Integer id = toInt(k);
            if (id != null) out.add(id);
        }
        return out;
    }

    return Collections.emptyList();
}

private static Integer toInt(Object v) {
    if (v == null) return null;
    if (v instanceof Integer) return (Integer) v;
    if (v instanceof Number) return ((Number) v).intValue();
    try {
        return Integer.parseInt(String.valueOf(v).trim());
    } catch (Exception e) {
        return null;
    }
}


    // =========================
    // TOP
    // =========================
    public static void dumpProjectFile(ProjectFile file, String title) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println("DUMP: " + title);
        System.out.println("============================================================");

        dumpProjectProperties(file);
        dumpProjectCustomPropertiesCompat(file);
        dumpCalendarsCompat(file);
        dumpResources(file);
        dumpTasks(file);
        dumpAssignments(file);
    }

    // =========================
    // PROJECT PROPERTIES
    // =========================
    public static void dumpProjectProperties(ProjectFile file) {
        System.out.println("\n--- PROJECT PROPERTIES ---");

        ProjectProperties p = file.getProjectProperties();
        if (p == null) {
            System.out.println("(project properties = null)");
            return;
        }

        safePrint("ID", p.getProjectID());
        safePrint("GUID", p.getGUID());
        safePrint("UNIQUEID", p.getUniqueID());
        safePrint("Name", p.getName());
        safePrint("Company", p.getCompany());
        safePrint("Manager", p.getManager());
        safePrint("Status Date", p.getStatusDate());
        safePrint("Start Date", p.getStartDate());
        safePrint("Finish Date", p.getFinishDate());
        safePrint("Default Calendar", file.getDefaultCalendar() != null ? file.getDefaultCalendar().getName() : null);

        safePrint("Currency Symbol", p.getCurrencySymbol());
        safePrint("Minutes Per Day", p.getMinutesPerDay());
        safePrint("Minutes Per Week", p.getMinutesPerWeek());
        safePrint("Days Per Month", p.getDaysPerMonth());
        safePrint("Fiscal Year Start Month", p.getFiscalYearStartMonth());
    }

    // =========================
    // CUSTOM PROPERTIES (compat)
    // =========================
    public static void dumpProjectCustomPropertiesCompat(ProjectFile file) {
        System.out.println("\n--- PROJECT CUSTOM PROPERTIES ---");

        ProjectProperties props = file.getProjectProperties();
        if (props == null) {
            System.out.println("(project properties = null)");
            return;
        }

        Object obj = invokeNoArg(props, "getCustomProperties");
        if (!(obj instanceof Map)) {
            System.out.println("(none / não suportado por esta build/reader)");
            return;
        }

        Map<?, ?> map = (Map<?, ?>) obj;
        if (map.isEmpty()) {
            System.out.println("(none)");
            return;
        }

        for (Map.Entry<?, ?> e : map.entrySet()) {
            System.out.println(String.valueOf(e.getKey()) + " = " + String.valueOf(e.getValue()));
        }
    }

    // =========================
    // CALENDARS (compat)
    // =========================
    public static void dumpCalendarsCompat(ProjectFile file) {
        List<ProjectCalendar> calendars = file.getCalendars();
        System.out.println("\n--- CALENDARS (" + (calendars == null ? 0 : calendars.size()) + ") ---");

        if (calendars == null || calendars.isEmpty()) {
            System.out.println("(none)");
            return;
        }

        for (ProjectCalendar cal : calendars) {
            System.out.println("\n[Calendar] " + nz(cal.getName()) + " | UID=" + cal.getUniqueID());

            // Week types (API varia)
            boolean any = dumpCalendarWeekTypesCompat(cal);
            if (!any) {
                System.out.println("  Week Types: (não foi possível determinar pela API desta build)");
            }
        }
    }

    private static boolean dumpCalendarWeekTypesCompat(ProjectCalendar cal) {
        System.out.println("  Week Types:");

        System.out.println("The calendar name is " + cal.getName());

        for (DayOfWeek day : DayOfWeek.values()) {
            String dayType = cal.getCalendarDayType(day).toString();
            System.out.println(day + " is a " + dayType + " day");
        }
        return true;
    }

    // =========================
    // RESOURCES
    // =========================
    public static void dumpResources(ProjectFile file) {
        List<Resource> resources = file.getResources();
        System.out.println("\n--- RESOURCES (" + (resources == null ? 0 : resources.size()) + ") ---");

        if (resources == null || resources.isEmpty()) {
            System.out.println("(none)");
            return;
        }

        for (Resource r : resources) {
            System.out.println("\n[Resource] " + nz(r.getName()) + " | ID=" + r.getID() + " | UID=" + r.getUniqueID());
            safePrint("  Type", r.getType());
            safePrint("  Max Units", r.getMaxUnits());
            safePrint("  Standard Rate", r.getStandardRate());
            safePrint("  Overtime Rate", r.getOvertimeRate());
            safePrint("  Cost Per Use", r.getCostPerUse());
            safePrint("  Calendar", r.getCalendar() != null ? r.getCalendar().getName() : null);
            safePrint("  Email", r.getEmailAddress());

            dumpResourceCustomFields(r, "  ");
        }
    }

    private static void dumpResourceCustomFields(Resource r, String indent) {
        for (int i = 1; i <= 30; i++) {
            String v = r.getText(i);
            if (v != null && !v.trim().isEmpty()) {
                System.out.println(indent + "Text" + i + ": " + v);
            }
        }
        for (int i = 1; i <= 20; i++) {
            Number v = r.getNumber(i);
            if (v != null) {
                System.out.println(indent + "Number" + i + ": " + v);
            }
        }
        for (int i = 1; i <= 10; i++) {
            LocalDateTime v = r.getDate(i);
            if (v != null) {
                System.out.println(indent + "Date" + i + ": " + v);
            }
        }
        for (int i = 1; i <= 20; i++) {
            Boolean v = r.getFlag(i);
            if (v != null) {
                System.out.println(indent + "Flag" + i + ": " + v);
            }
        }
    }

    // =========================
    // TASKS
    // =========================
    public static void dumpTasks(ProjectFile file) {
        List<Task> tasks = file.getTasks();
        System.out.println("\n--- TASKS (" + (tasks == null ? 0 : tasks.size()) + ") ---");

        if (tasks == null || tasks.isEmpty()) {
            System.out.println("(none)");
            return;
        }

        for (Task t : tasks) {
            if (t == null) {
                continue;
            }

            System.out.println("\n[Task] " + nz(t.getName()) + " | ID=" + t.getID() + " | UID=" + t.getUniqueID());

            safePrint("  Name", t.getName());
            safePrint("  ID", t.getID());
            safePrint("  UID", t.getUniqueID());
            safePrint("  WBS", t.getWBS());
            safePrint("  Outline Level", t.getOutlineLevel());
            safePrint("  Parent", t.getParentTask() != null ? t.getParentTask().getName() : null);

            safePrint("  Start", t.getStart());
            safePrint("  Finish", t.getFinish());
            safePrint("  Duration", t.getDuration());
            safePrint("  Constraint Type", t.getConstraintType());
            safePrint("  Constraint Date", t.getConstraintDate());

            safePrint("  % Complete", t.getPercentageComplete());
            safePrint("  Work", t.getWork());
            safePrint("  Cost", t.getCost());

            safePrint("  Calendar", t.getCalendar() != null ? t.getCalendar().getName() : null);

            safePrint("  BL Start", t.getBaselineStart());
            safePrint("  BL Finish", t.getBaselineFinish());
            safePrint("  BL Duration", t.getBaselineDuration());
            safePrint("  BL Cost", t.getBaselineCost());

            dumpTaskLinksCompat(t);
            dumpTaskCustomFields(t, "  ");
        }
    }

    private static void dumpTaskLinksCompat(Task t) {
        System.out.println("  Predecessors:");
        printRelationsList(invokeNoArg(t, "getPredecessors"), true);

        System.out.println("  Successors:");
        printRelationsList(invokeNoArg(t, "getSuccessors"), false);
    }

    private static void printRelationsList(Object listObj, boolean isPredecessorList) {
        if (!(listObj instanceof List)) {
            System.out.println("    (none)");
            return;
        }
        List<?> list = (List<?>) listObj;
        if (list.isEmpty()) {
            System.out.println("    (none)");
            return;
        }

        for (Object rel : list) {
            Object type = invokeNoArg(rel, "getType");
            Object lag = invokeNoArg(rel, "getLag");

            Object otherTaskObj = isPredecessorList
                    ? invokeNoArg(rel, "getTargetTask")
                    : invokeNoArg(rel, "getSourceTask");

            String otherName = null;
            if (otherTaskObj instanceof Task) {
                otherName = ((Task) otherTaskObj).getName();
            } else if (otherTaskObj != null) {
                Object nm = invokeNoArg(otherTaskObj, "getName");
                otherName = nm != null ? String.valueOf(nm) : String.valueOf(otherTaskObj);
            }

            System.out.println("    " + (otherName == null ? "(unknown task)" : otherName)
                    + " | type=" + (type == null ? "(unknown)" : type)
                    + " | lag=" + (lag == null ? "(unknown)" : lag));
        }
    }

    private static void dumpTaskCustomFields(Task t, String indent) {
        for (int i = 1; i <= 30; i++) {
            String v = t.getText(i);
            if (v != null && !v.trim().isEmpty()) {
                System.out.println(indent + "Text" + i + ": " + v);
            }
        }
        for (int i = 1; i <= 20; i++) {
            Number v = t.getNumber(i);
            if (v != null) {
                System.out.println(indent + "Number" + i + ": " + v);
            }
        }
        for (int i = 1; i <= 10; i++) {
            LocalDateTime v = t.getDate(i);
            if (v != null) {
                System.out.println(indent + "Date" + i + ": " + v);
            }
        }
        for (int i = 1; i <= 20; i++) {
            Boolean v = t.getFlag(i);
            if (v != null) {
                System.out.println(indent + "Flag" + i + ": " + v);
            }
        }
        for (int i = 1; i <= 10; i++) {
            Duration v = t.getDuration(i);
            if (v != null) {
                System.out.println(indent + "Duration" + i + ": " + v);
            }
        }
        for (int i = 1; i <= 10; i++) {
            Number v = t.getCost(i);
            if (v != null) {
                System.out.println(indent + "Cost" + i + ": " + v);
            }
        }
    }

    // =========================
    // ASSIGNMENTS
    // =========================
    public static void dumpAssignments(ProjectFile file) {
        List<ResourceAssignment> ras = file.getResourceAssignments();
        System.out.println("\n--- ASSIGNMENTS (" + (ras == null ? 0 : ras.size()) + ") ---");

        if (ras == null || ras.isEmpty()) {
            System.out.println("(none)");
            return;
        }

        for (ResourceAssignment ra : ras) {
            Task t = ra.getTask();
            Resource r = ra.getResource();

            System.out.println("\n[Assignment]");
            safePrint("  Task", t != null ? t.getName() : null);
            safePrint("  Resource", r != null ? r.getName() : null);
            safePrint("  Units", ra.getUnits());
            safePrint("  Work", ra.getWork());
            safePrint("  Cost", ra.getCost());
            safePrint("  Start", ra.getStart());
            safePrint("  Finish", ra.getFinish());
        }
    }

    // =========================
    // BASELINES (compat)
    // =========================
    public static void dumpBaselinesCompat(ProjectFile file) {
        System.out.println("\n============================================================");
        System.out.println("BASELINES");
        System.out.println("============================================================");

        Object obj = invokeNoArg(file, "getBaselines");
        if (!(obj instanceof Map)) {
            System.out.println("(nenhuma baseline encontrada / não suportado por este reader/build)");
            return;
        }

        Map<?, ?> baselines = (Map<?, ?>) obj;
        if (baselines.isEmpty()) {
            System.out.println("(nenhuma baseline encontrada)");
            return;
        }

        for (Map.Entry<?, ?> e : baselines.entrySet()) {
            Object key = e.getKey();
            Object value = e.getValue();

            if (value instanceof ProjectFile) {
                dumpProjectFile((ProjectFile) value, "BASELINE ID=" + String.valueOf(key));
            } else {
                System.out.println("Baseline " + key + " = " + value + " (tipo inesperado)");
            }
        }
    }

    // =========================
    // REFLECTION HELPERS
    // =========================
    private static Object invokeNoArg(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            return m.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object invokeOneArg(Object target, String methodName, Class<?> argType, Object argValue) {
        try {
            Method m = target.getClass().getMethod(methodName, argType);
            return m.invoke(target, argValue);
        } catch (Exception e) {
            return null;
        }
    }

    // =========================
    // SMALL HELPERS
    // =========================
    private static void safePrint(String label, Object value) {
        System.out.println(label + ": " + (value == null ? "(null)" : value));
    }

    private static String nz(String s) {
        return s == null ? "(null)" : s;
    }
}

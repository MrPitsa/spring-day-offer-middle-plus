package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Primary
public class TaskDistributorImpl implements TaskDistributor {

    public static final int MAX_LEAD_TIME = 7 * 60;
    public static final int MIN_LEAD_TIME = 6 * 60;

    @Override
    public void distribute(List<EmployeeDTO> employees, List<TaskDTO> tasks) {

        // will distribute high priority short tasks first
        Map<TaskDTO, Boolean> sortedTasks = tasks.stream()
                .sorted(Comparator.comparingInt(TaskDTO::getPriority).thenComparingInt(TaskDTO::getLeadTime))
                .collect(Collectors.toMap(Function.identity(), t -> false, (a, b) -> a, LinkedHashMap::new));

        // all employees and their available time
        Map<EmployeeDTO, Integer> availability = employees.stream()
                .collect(Collectors.toMap(Function.identity(), e -> MAX_LEAD_TIME - e.getTotalLeadTime()));

        List<EmployeeDTO> availableEmployees = new ArrayList<>(availability.keySet());

        for (TaskDTO task : sortedTasks.keySet()) {
            var availableEmployeesIterator = availableEmployees.iterator();
            while (availableEmployeesIterator.hasNext()) {
                EmployeeDTO employee = availableEmployeesIterator.next();
                int availableTime = availability.get(employee);
                if (availableTime - task.getLeadTime() < 0) {
                    continue;
                }
                employee.getTasks().add(task);
                sortedTasks.put(task, true);
                availability.put(employee, availableTime - task.getLeadTime());
                if (availability.get(employee) == 0) {
                    availability.remove(employee);
                    availableEmployeesIterator.remove();
                }
            }
        }

        // employees with lead time less then 6 hours
//        availability = availability.entrySet().stream()
//                .filter(e -> e.getKey().getTotalLeadTime() < MIN_LEAD_TIME)
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        availableEmployees = new ArrayList<>(availability.keySet());

        // TODO: try to fill employee workload up to 6 hours total
//        for (EmployeeDTO employee : availableEmployees) {
//            employee.getTasks().stream().sorted(
//                    Comparator.comparingInt(TaskDTO::getLeadTime).reversed())
//        }
    }
}

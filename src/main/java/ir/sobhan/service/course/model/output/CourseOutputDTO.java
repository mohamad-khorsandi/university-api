package ir.sobhan.service.course.model.output;

import ir.sobhan.service.AbstractService.model.output.OutPutDTO;
import ir.sobhan.service.course.model.entity.Course;
import lombok.Getter;

@Getter
public class CourseOutputDTO extends OutPutDTO<Course> {
    public Long id;
    public String title;
    public Integer units;

    public CourseOutputDTO(Course realObj) {
        super(realObj);
    }
}

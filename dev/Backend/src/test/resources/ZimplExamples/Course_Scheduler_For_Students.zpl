# Course Scheduler - Student Schedule Optimization Model

param toString[{0..9}] := <0> "0", <1> "1", <2> "2", <3> "3", <4> "4", <5> "5", <6> "6", <7> "7", <8> "8", <9> "9";
param stringToNumber[{"0","1","2","3","4","5","6","7","8","9"}] := <"0"> 0, <"1"> 1, <"2"> 2, <"3"> 3, <"4"> 4, <"5"> 5, <"6"> 6, <"7"> 7, <"8"> 8, <"9"> 9;

defnumb convertStringToNumber(str) := 
    sum <i> in {0..length(str)-1} : (stringToNumber[substr(str,length(str) - 1 - i,1)] * (10**i));

defstrg convertNumberToString(n) :=
    if n < 0 then
        "-" + convertNumberToString(-n)
    else if n < 10 then
        toString[n]
    else
        convertNumberToString(n div 10) + toString[n mod 10]
    end end;


# Sets for basic data structures
set Weekdays := {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
set SessionTypes := {"Lecture", "Practice", "Lab"};

# Course information
set CourseData := {
    # <course_name, department, academic_points, is_mandatory>
    <"Calculus 1", "Mathematics", 5, 1>,
    <"Linear Algebra", "Mathematics", 4, 1>,
    <"Introduction to CS", "Computer Science", 4, 1>,
    <"Physics 1", "Physics", 4, 0>,
    <"Digital Systems", "Computer Science", 3, 0>,
    <"Discrete Mathematics", "Mathematics", 4, 0>,
    <"Data Structures", "Computer Science", 4, 0>,
    <"Statistics", "Mathematics", 3, 0>, 
    <"SPL", "Computer Science", 5, 0>,
    <"Introduction to Software Engineering", "Computer Science", 5, 0>,
    <"Introduction to Artificial Intelligence", "Computer Science", 5, 0>,
    <"Operating Systems", "Computer Science", 5, 0>,
    <"Computer Networks", "Computer Science", 3.5, 0>,
    <"Computer Architecture", "Computer Science", 3.5, 0>,
    <"Computer Graphics", "Computer Science", 2, 0>,
    <"Computer Vision", "Computer Science", 3, 0>,
    <"Computer Security", "Computer Science", 2.5, 0>
};

# Extract course names for easier reference
set Courses := proj(CourseData, <1>);

# Course schedule information
set CourseSchedule := {
    # <course_name, group_num, teacher, session_type, weekday, start_hour, end_hour>

    <"Calculus 1", 1, "Dr. Smith", "Lecture", "Sunday", 9, 12>,
    <"Calculus 1", 1, "Mr. Johnson", "Practice", "Tuesday", 13, 15>,
    <"Calculus 1", 2, "Dr. Smith", "Lecture", "Monday", 10, 13>,
    <"Calculus 1", 2, "Mr. Johnson", "Practice", "Wednesday", 15, 17>,
    
    <"Linear Algebra", 1, "Prof. Brown", "Lecture", "Monday", 9, 11>,
    <"Linear Algebra", 1, "Ms. Davis", "Practice", "Wednesday", 11, 13>,
    <"Linear Algebra", 2, "Prof. Brown", "Lecture", "Tuesday", 11, 13>,
    <"Linear Algebra", 2, "Ms. Davis", "Practice", "Thursday", 9, 11>,
    
    <"Introduction to CS", 1, "Dr. Wilson", "Lecture", "Sunday", 12, 15>,
    <"Introduction to CS", 1, "Dr. Wilson", "Lab", "Monday", 14, 17>,
    <"Introduction to CS", 2, "Dr. Wilson", "Lecture", "Wednesday", 8, 11>,
    <"Introduction to CS", 2, "Dr. Wilson", "Lab", "Thursday", 12, 15>,

    <"Physics 1", 1, "Dr. Smith", "Lecture", "Sunday", 11, 13>,
    <"Physics 1", 1, "Dr. Smith", "Lab", "Monday", 13, 15>,
    <"Physics 1", 2, "Dr. Smith", "Lecture", "Wednesday", 9, 11>,
    <"Physics 1", 2, "Dr. Smith", "Lab", "Thursday", 8, 10>,

    <"Digital Systems", 1, "Dr. Smith", "Lecture", "Sunday", 8, 10>,
    <"Digital Systems", 1, "Dr. Smith", "Lab", "Monday", 10, 12>,
    <"Digital Systems", 2, "Dr. Smith", "Lecture", "Wednesday", 8, 10>,
    <"Digital Systems", 2, "Dr. Smith", "Lab", "Thursday", 10, 12>,

    <"Discrete Mathematics", 1, "Dr. Smith", "Lecture", "Sunday", 9, 11>,
    <"Discrete Mathematics", 1, "Dr. Smith", "Lab", "Monday", 11, 13>,
    <"Discrete Mathematics", 2, "Dr. Smith", "Lecture", "Wednesday", 9, 11>,
    <"Discrete Mathematics", 2, "Dr. Smith", "Lab", "Thursday", 13, 15>,

    <"Data Structures", 1, "Dr. Smith", "Lecture", "Sunday", 10, 13>,
    <"Data Structures", 1, "Dr. Smith", "Practice", "Monday", 10, 13>,
    <"Data Structures", 2, "Dr. Smith", "Lecture", "Wednesday", 8, 11>,
    <"Data Structures", 2, "Dr. Smith", "Practice", "Thursday", 12, 15>,

    <"Statistics", 1, "Dr. Smith", "Lecture", "Sunday", 10, 13>,
    <"Statistics", 1, "Dr. Smith", "Practice", "Monday", 10, 13>,
    <"Statistics", 2, "Dr. Smith", "Lecture", "Wednesday", 10, 13>,
    <"Statistics", 2, "Dr. Smith", "Practice", "Thursday", 12, 15>,

    <"SPL", 1, "Dr. Smith", "Lecture", "Sunday", 15, 17>,
    <"SPL", 1, "Dr. Smith", "Practice", "Monday", 18, 20>,
    <"SPL", 2, "Dr. Smith", "Lecture", "Wednesday", 16, 18>,
    <"SPL", 2, "Dr. Smith", "Practice", "Thursday", 18, 20>,

    <"Introduction to Software Engineering", 1, "Dr. Smith", "Lecture", "Sunday", 13, 15>,
    <"Introduction to Software Engineering", 1, "Dr. Smith", "Practice", "Tuesday", 16, 18>,
    <"Introduction to Software Engineering", 2, "Dr. Smith", "Lecture", "Sunday", 17, 19>,
    <"Introduction to Software Engineering", 2, "Dr. Smith", "Practice", "Tuesday", 13, 15>,

    <"Introduction to Artificial Intelligence", 1, "Dr. Smith", "Lecture", "Sunday", 13, 15>,
    <"Introduction to Artificial Intelligence", 1, "Dr. Smith", "Practice", "Thursday", 15, 17>,
    <"Introduction to Artificial Intelligence", 2, "Dr. Smith", "Lecture", "Monday", 9, 11>,
    <"Introduction to Artificial Intelligence", 2, "Dr. Smith", "Practice", "Thursday", 13, 15>,

    <"Operating Systems", 1, "Dr. Smith", "Lecture", "Sunday", 13, 15>,
    <"Operating Systems", 1, "Dr. Smith", "Practice", "Monday", 15, 17>,
    <"Operating Systems", 2, "Dr. Smith", "Lecture", "Sunday", 9, 11>,
    <"Operating Systems", 2, "Dr. Smith", "Practice", "Monday", 13, 15>,
    
    <"Computer Networks", 1, "Dr. Smith", "Lecture", "Tuesday", 13, 15>,
    <"Computer Networks", 1, "Dr. Smith", "Lab", "Wednesday", 15, 17>,
    <"Computer Networks", 2, "Dr. Smith", "Lecture", "Wednesday", 9, 11>,
    <"Computer Networks", 2, "Dr. Smith", "Lab", "Thursday", 13, 15>,

    <"Computer Architecture", 1, "Dr. Smith", "Lecture", "Sunday", 13, 15>,
    <"Computer Architecture", 1, "Dr. Smith", "Lab", "Thursday", 15, 17>,
    <"Computer Architecture", 2, "Dr. Smith", "Lecture", "Monday", 9, 11>,
    <"Computer Architecture", 2, "Dr. Smith", "Lab", "Thursday", 13, 15>,
    
    <"Computer Graphics", 1, "Dr. Smith", "Lecture", "Wednesday", 13, 15>,
    <"Computer Graphics", 1, "Dr. Smith", "Lab", "Thursday", 15, 17>,
    <"Computer Graphics", 2, "Dr. Smith", "Lecture", "Sunday", 9, 11>,
    <"Computer Graphics", 2, "Dr. Smith", "Lab", "Sunday", 13, 15>,

    <"Computer Vision", 1, "Dr. Smith", "Lecture", "Sunday", 13, 17>,
    <"Computer Vision", 2, "Dr. Smith", "Lecture", "Wednesday", 9, 13>,

    <"Computer Security", 1, "Dr. Smith", "Lecture", "Sunday", 13, 15>,
    <"Computer Security", 1, "Dr. Smith", "Lab", "Monday", 15, 17>,
    <"Computer Security", 2, "Dr. Smith", "Lecture", "Wednesday", 8, 12>
};

# Parameters from CourseData
defnumb getPoints(course) := ord({ <co,pts> in proj(CourseData, <1,3>) | co == course} union {<"",0>},1,2);
defnumb is_mandatory(course) := ord({ <co,is_man> in proj(CourseData, <1,4>) | co == course} union {<"",0>},1,2);


# Target academic points for the semester
param target_points := 21;

# Decision Variables
var take_course[Courses] binary;  # 1 if student takes the course
var choose_group[<c, g> in proj(CourseSchedule, <1, 2>)] binary;  # 1 if student chooses specific group

# Helper variable for counting active days
var day_has_class[Weekdays] binary;  # 1 if there are any classes on that day

var first_activity_of_the_day[<c, g, w> in proj(CourseSchedule, <1, 2,5>)] binary;

var total_points real >= 0;
defstrg FormatCourseSchedulePresentation(c,g,t) := c + ", " + convertNumberToString(g) + ", " + t;
set CourseScheduleFormatted := {<c,g,t,st,wd,sh,eh> in CourseSchedule : <FormatCourseSchedulePresentation(c,g,t)>};
set preAssign_presentation := {<8,"Monday", "Calculus 1", 1, "Dr. Smith">};
var assignment_presentation[{8..20} * Weekdays * proj(CourseSchedule, <1,2,3>)] binary;
var assignment_presentation_formatted[{8..20} * Weekdays * CourseScheduleFormatted] binary;

# Constraints

subto assignment_presentation_constraints:
    forall <c,g,t,st,wd,sh,eh, h> in CourseSchedule * {8..20} | h >= sh and h < eh:
        assignment_presentation[h,wd,c,g,t] == assignment_presentation_formatted[h,wd,FormatCourseSchedulePresentation(c,g,t)] and assignment_presentation[h,wd,c,g,t] == choose_group[c,g];

# Must take all mandatory courses
subto mandatory_courses:
    forall <c> in Courses | is_mandatory(c) == 1:
        take_course[c] == 1;

# If taking a course, must choose exactly one group
subto group_selection:
    forall <c> in Courses:
        (sum <c2,g2> in proj(CourseSchedule, <1,2>) | c == c2 : choose_group[c2,g2]) == take_course[c];

# Time collision prevention
# For each weekday and each hour, ensure no more than one session is scheduled
subto no_collisions:
    forall <w> in Weekdays:
        forall <h> in {0 .. 23}:
            sum <c, g, t, st, wd, sh, eh> in CourseSchedule | wd == w and h >= sh and h < eh:
                choose_group[c,g] <= 1;

# Set day_has_class variable
subto active_days:
    forall <w> in Weekdays:
        (sum <c, g, t, st, wd, sh, eh> in CourseSchedule | wd == w:
            choose_group[c,g]) <= card(CourseSchedule) * day_has_class[w];

# # Calculate total points
subto calculateTotalPoints:
    total_points == (sum <c> in Courses: getPoints(c) * take_course[c]);

subto first_activity_of_the_day:
    forall <c,g,t,st,wd,sh,eh> in CourseSchedule:
        vif choose_group[c,g] == 1 and (sum <c2,g2,t2,st2,wd2,sh2,eh2> in CourseSchedule | wd == wd2 and sh2 < sh: choose_group[c2,g2]) == 0 then
            first_activity_of_the_day[c,g,wd] == 1 else first_activity_of_the_day[c,g,wd] == 0 end;

#<course_name, rating>
set preffered_courses := {
    <"Physics 1", 5>,
    <"Digital Systems", 4>,
    <"Introduction to Software Engineering", 2>,
    <"Introduction to Artificial Intelligence", 1>
};
param sumOfPrefferedCoursesRatings := sum <c, rating> in preffered_courses : abs(rating);

defnumb getCourseRating(course) := ord({ <co, rating> in preffered_courses | co == course} union {<"ds",0>},1,2);

#<teacher_name, rating>
set preffered_teachers := {
    <"Dr. Smith", 10>,
    <"Mr. Johnson", 1>,
    <"Prof. Brown", 1>,
    <"Ms. Davis", 1>,
    <"Dr. Wilson", 1>
};

param sumOfPrefferedTeachersRatings := sum <t, rating> in preffered_teachers : abs(rating);

defnumb getTeacherRating(teacher) := ord({ <te, rating> in preffered_teachers | te == teacher} union {<"0",0>},1,2);

# Weight parameters
param weight_points := 100; # higher means aim closer to target points
param weight_days := 1; # higher means more off days
param weight_day_start_early := 1; # higher positive means start days early, low negative start days late, 0 means agnostic.
param weight_preffered_courses := 1; # higher means more preffered courses
param weight_preffered_teachers := 1; # higher means get preffered teachers more often


minimize objective:
    weight_points * abs(total_points - target_points) + 
    weight_days * (sum <w> in Weekdays: day_has_class[w]) +
    weight_day_start_early * (sum <c,g,w> in proj(CourseSchedule, <1,2,5>): (first_activity_of_the_day[c,g,w] * (min <c2,g2,t2,st2,wd2,sh2,eh2> in CourseSchedule | c == c2 and g == g2 and w == wd2: sh2))) +
    ((-1 * weight_preffered_courses) * (sum <c> in Courses: (take_course[c] * getCourseRating(c)/sumOfPrefferedCoursesRatings))) +
    ((-1 * weight_preffered_teachers) * (sum <c> in Courses: (take_course[c] * getTeacherRating(c)/sumOfPrefferedTeachersRatings)));
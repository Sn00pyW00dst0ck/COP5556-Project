program nested_classes;

type
    TTeacher = class
    public
        Name: String;

        constructor Create(name: String);
    end;

    TStudent = class
    public
        Name: String;
        Teacher: TTeacher;

        constructor Create(name: String; teacher: TTeacher);
    end;

constructor TTeacher.Create(name: String);
begin
    Self.Name := name;
end;

constructor TStudent.Create(name: String; teacher: TTeacher);
begin
    Self.Name := name;
    Self.Teacher := teacher;
end;

var
    Person1: TTeacher;
    Person2: TStudent;

begin
    Person1 := TTeacher.Create('Omni-Man');
    Person2 := TStudent.Create('Invincible', Person1);

    writeln(Person1.Name);
    writeln(Person2.Name);
    writeln(Person2.Teacher.Name);
end.
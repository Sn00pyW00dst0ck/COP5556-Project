program simple_class;

type
    TPerson = class
    public
        Name: String;
        Age: Integer;

        constructor Create;
        destructor Destroy;
        procedure greet();
    end;

constructor TPerson.Create;
begin
    writeln('CONSTRUCTOR');
end;

destructor TPerson.Destroy;
begin
    writeln('DESTRUCTOR');
end;

procedure TPerson.greet();
begin
    writeln('Hello');
end;

var
    Person1: TPerson;

begin
    Person1 := TPerson.Create;

    writeln(Person1.Name);
    writeln(Person1.Name.Age(Person1));
end.

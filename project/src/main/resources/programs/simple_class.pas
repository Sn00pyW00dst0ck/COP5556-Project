program simple_class;

type
    TPerson = class
    public
        Name: String;
        Age: Integer;

        constructor Create;
        destructor Destroy;
        procedure greet();
        function getAge(): Integer;
    end;

constructor TPerson.Create;
begin
    writeln('CONSTRUCTOR');
    Self.Name := 'Test Student';
    Self.Age := 21;
end;

destructor TPerson.Destroy;
begin
    writeln('DESTRUCTOR');
end;

procedure TPerson.greet();
begin
    writeln('Hello from ' + Self.Name);
    Self.Age := 21;
end;

function TPerson.getAge(): Integer;
begin
    result := Self.Age;
end;

var
    Person1: TPerson;

begin
    Person1 := TPerson.Create;
    Person1.Age := 10;

    writeln(Person1.Age);
    Person1.greet();
    writeln(Person1.Age);
    writeln(Person1.getAge());
end.

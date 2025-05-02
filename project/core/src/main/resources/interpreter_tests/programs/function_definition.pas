program function_definition;

var 
    result : integer;

function Sum(a, b, c : integer): integer;
begin
    result := a + b + c
end;

function GetMessage: string;
begin
    result := 'Hello from GetMessage';
end;

function SumByRef(a, b, c : integer; var m : integer): integer;
begin
    m := a + b + c;
    result := m;
end;

begin
    writeln(Sum(67, 45, 15));
    writeln(GetMessage());

    result := 0;
    SumByRef(10, 20, 45, result);
    writeln(result);
end.
program function_definition;

function Sum(a, b, c : integer): integer;
begin
    result := a + b + c
end;

function GetMessage: string;
begin
    result := 'Hello from GetMessage';
end;

begin
    writeln(Sum(67, 45, 15));
    writeln(GetMessage());
end.
program function_definition;

function Sum(a, b, c : integer): integer;
begin
    Result := a + b + c
end;

function GetMessage(): string;
begin
    Result := 'Hello from GetMessage';
end;

begin
    writeln(Sum(15, 45, 67));
    writeln(GetMessage());
end.
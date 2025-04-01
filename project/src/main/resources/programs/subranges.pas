Program Subranges;

type
	TSmallNum = 0..9;
	TLowercase = 'a'..'z';
	TDay = (Mon, Tue, Wed, Thu, Fri, Sat, Sun);
	TWeekDays = Mon..Fri;

var
	smallNum : TSmallNum;
	letter : TLowercase;
	day : TWeekDays;

begin
	smallNum := 5;
	letter := 'b';
	day := Mon;

	writeln( smallNum );
	writeln( letter );
	writeln( day );
end.

